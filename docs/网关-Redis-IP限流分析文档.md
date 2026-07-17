# 网关 + Redis IP 限流分析文档（初学者复盘版）

> 对应模块：`guo-gateway`  
> 核心类：`com.test.guo.gateway.filter.IpRateLimitFilter`  
> 规则：同一客户端 IP，对同一 getById 接口（`GET /users/{数字}`），在时间窗口内请求次数不能超过 3 次。  
> 本文目标：从最基础语法讲清底层逻辑，方便日后复盘。

---

## 目录

1. [这一板块在解决什么问题](#1-这一板块在解决什么问题)
2. [前置概念：Redis 与网关](#2-前置概念redis-与网关)
3. [和「部门缓存」的区别](#3-和部门缓存的区别)
4. [整体架构与请求链路](#4-整体架构与请求链路)
5. [需要用到的 Java 基础语法清单](#5-需要用到的-java-基础语法清单)
6. [类结构总览](#6-类结构总览)
7. [逐块代码精讲](#7-逐块代码精讲)
8. [一次请求的完整执行时序](#8-一次请求的完整执行时序)
9. [构造方法 vs 普通方法（本类中的用法）](#9-构造方法-vs-普通方法本类中的用法)
10. [异步写法（Mono / flatMap）如何对应同步逻辑](#10-异步写法mono--flatmap如何对应同步逻辑)
11. [测试与排错](#11-测试与排错)
12. [面试常问提炼](#12-面试常问提炼)
13. [自测题与答案](#13-自测题与答案)
14. [复盘检查清单](#14-复盘检查清单)

---

## 1. 这一板块在解决什么问题

### 1.1 业务规则

| 维度 | 规则 |
|------|------|
| 谁 | 同一个用户 IP |
| 哪个接口 | `GET /users/{id}`（用户详情 getById） |
| 多长时间 | 时间窗口（代码里为 `Duration.ofSeconds(10)`，学习阶段可改；题目原始要求常为 1 秒） |
| 多少次 | 不能超过 3 次 |
| 超限怎么办 | HTTP 429，JSON 提示「请求过于频繁，请稍后再试」，不转发到 user-service |

### 1.2 为什么放在网关，而不是 UserController

| 放网关 | 放业务服务 |
|--------|------------|
| 统一入口，一次实现保护多个后端 | 每个服务都要写一遍 |
| 超限请求进不了 user-service，更省资源 | 请求已打到业务进程 |
| 和路由、鉴权等横切能力放一起 | 业务代码被安全逻辑污染 |

一句话：**限流是“门口保安”，不是“柜员办业务”。**

---

## 2. 前置概念：Redis 与网关

### 2.1 Redis 是什么

Redis 是内存中的 **Key-Value（键值）** 存储。

| 对比 | MySQL | Redis |
|------|-------|-------|
| 主要用途 | 持久业务数据 | 缓存、计数、分布式锁、限流 |
| 速度 | 相对慢（落盘） | 极快（内存） |
| 本需求用法 | 不直接参与限流计数 | 用 String + INCR 记次数，并设置过期 |

### 2.2 为什么限流要用 Redis

1. **快**：高频加减次数合适。  
2. **带过期（TTL）**：Key 到期自动删除 = 时间窗口自然结束。  
3. **可共享**：以后网关多实例时，计数仍一致（本地 `Map` 做不到跨实例）。

### 2.3 Redis 基本类型与本需求选型

| 类型 | 典型用途 | 本需求是否采用 |
|------|----------|----------------|
| String | 缓存 JSON、计数、分布式锁 | **采用**（存次数） |
| Hash | 对象多字段 | 否 |
| List | 简单队列 | 否 |
| Set | 去重集合 | 否 |
| ZSet | 排行榜 | 否 |

**采用 String 的原因：** 只要一个数字；`INCR` 原子加一，写法简单，面试最常见。

核心命令对应关系：

| Redis 命令 | 在本项目中的含义 |
|------------|------------------|
| `INCR key` | 次数 +1，返回当前次数 |
| `EXPIRE key 秒数` | 设置过期，窗口开始 |
| Key 过期删除 | 窗口结束，下次从 1 重新计 |

### 2.4 网关是什么

API 网关 = 微服务的**统一入口**（本项目 `guo-gateway`，端口 **8088**）。

```text
客户端
  → 只访问网关 8088
      → /users/**        → user-service（8081）
      → /departments/**  → dept-service（8082）
```

网关常见职责：路由、鉴权、限流、日志、负载均衡（`lb://服务名`）。

本项目网关基于 **Spring Cloud Gateway（WebFlux）**，因此 Redis 客户端使用 **Reactive**（`ReactiveStringRedisTemplate`），不能照搬 dept-service 里阻塞的 `StringRedisTemplate`。

---

## 3. 和「部门缓存」的区别

两者都可能用 Redis，但目的完全不同。

| | 部门缓存（dept-service） | IP 限流（gateway） |
|--|-------------------------|---------------------|
| 目的 | 少查数据库，读得更快 | 控制访问频率，保护系统 |
| Redis 存什么 | 部门数据（JSON） | 次数（1、2、3…） |
| Key 举例 | `dept:id:1` | `rate:limit:127.0.0.1:GET:/users/1` |
| 超限/未命中时 | 回源查库再写入 | **拒绝请求（429）** |
| 类比 | 公告栏（信息贴出来） | 保安（进太多次拦住） |

关系：可以同时存在；限流管「进不进门」，缓存管「进门后信息从哪拿」。

---

## 4. 整体架构与请求链路

```text
Postman / 浏览器
       │
       ▼
 guo-gateway :8088
       │
       ▼
 IpRateLimitFilter（GlobalFilter）
       │
       ├─ 不是 GET /users/{数字} ──► 直接放行（chain.filter）
       │
       └─ 是 getById
             │
             ▼
        Redis INCR（按 IP+路径计数）
             │
             ├─ count <= 3 ──► chain.filter ──► user-service :8081
             │
             └─ count >  3 ──► 写 429 JSON（不调用 chain）
```

**重要：** 直连 `http://localhost:8081/users/1` **不会**走限流；必须走 `http://localhost:8088/users/1`。

---

## 5. 需要用到的 Java 基础语法清单

复盘时若某段看不懂，先回到本表。

### 5.1 类型、变量、方法（最易混）

```java
ServerHttpRequest request = exchange.getRequest();
//  │类型名            │变量名   │方法调用
```

| 位置 | 是什么 | 例子 |
|------|--------|------|
| 行首 | **类型**（什么盒子） | `String`、`HttpMethod`、`Logger` |
| 中间 | **变量名**（盒子叫啥） | `path`、`method`、`log` |
| 带 `()` 的 | **方法**（在干活） | `getRequest()`、`getPath()` |

### 5.2 修饰符

| 词 | 含义 |
|----|------|
| `private` | 只有本类能用 |
| `public` | 别的类/框架也能调用 |
| `static` | 属于类，不依赖某个对象实例 |
| `final` | 赋值后不再改（常量或固定引用） |

### 5.3 类与接口

| 语法 | 含义 |
|------|------|
| `class A` | 定义类（图纸） |
| `implements B` | 实现接口，必须写接口要求的方法 |
| `@Component` | 交给 Spring 创建并管理 |
| `@Override` | 标明实现/重写接口或父类方法 |

### 5.4 构造方法 vs 普通方法

| | 构造方法 | 普通方法 |
|--|----------|----------|
| 名字 | **必须和类名相同** | 自己起名 |
| 返回类型 | 不写 | 要写 `void` / `String` / `Mono`… |
| 何时执行 | `new` / Spring 创建 Bean 时 | 对象存在后按需调用 |
| 次数 | 每个对象创建时通常 1 次 | 可很多次 |
| 本类例子 | `IpRateLimitFilter(redisTemplate)` | `filter`、`resolveClientIp`… |

记忆：构造 = 开店进货；普通方法 = 客人来了再接待。

### 5.5 运算符与流程

| 语法 | 含义 |
|------|------|
| `!=` | 不等于 |
| `==` | 等于 |
| `&&` | 并且 |
| `\|\|` | 或者 |
| `!` | 取反 |
| `if (...) { }` | 条件成立才执行 |
| `return` | 返回结果并结束当前方法 |
| `条件 ? A : B` | 三元运算：真用 A，假用 B |

### 5.6 链式调用

```java
request.getURI().getPath();
// 先 getURI()，再在结果上 getPath()
```

### 5.7 Lambda（箭头）

```java
count -> {  /* 用 count 做事 */  }
```

含义：一个**没有名字的小函数**，参数叫 `count`。  
常写在 `flatMap(count -> { ... })` 里，表示「结果到了再执行」。

### 5.8 Mono（反应式，先记一句）

```text
Mono<T> ≈ 「以后会有一个 T（或完成一件事）」
```

网关是 WebFlux，Redis 操作也是异步的，所以用 `Mono` + `flatMap` 串联步骤，而不是普通的「一行写完立刻有返回值」。

| API | 白话 |
|-----|------|
| `flatMap(x -> { ... })` | 等上一步结果 `x` 到了，再做下一步 |
| `Mono.just(true)` | 立刻得到一个已完成的 Mono（占位） |
| `thenReturn(值)` | 等当前 Mono 完成，把「值」传给下一步 |
| `Mono.just(buffer)` | 立刻包装一个 buffer |

---

## 6. 类结构总览

文件：`guo-gateway/src/main/java/com/test/guo/gateway/filter/IpRateLimitFilter.java`

```text
IpRateLimitFilter
│
├── 【字段】准备工作
│     log                 → 打日志
│     USER_GET_BY_ID      → 路径正则
│     LIMIT = 3           → 最大次数
│     WIDOW = 10秒        → 时间窗口（变量名本意是 WINDOW）
│     redisTemplate       → Redis 工具
│
├── 【构造方法】
│     IpRateLimitFilter(redisTemplate) → 注入 Redis
│
├── 【普通方法 - 主流程】
│     filter(exchange, chain) → 每个请求入口
│
├── 【普通方法 - 工具】
│     resolveClientIp(request)           → 解析 IP
│     writeTooManyRequests(response)     → 写 429
│
└── 【普通方法 - 排序】
      getOrder() → 返回 -100（越小越先执行）
```

---

## 7. 逐块代码精讲

### 7.1 类声明

```java
@Component
public class IpRateLimitFilter implements GlobalFilter, Ordered {
```

- `@Component`：没有它，Spring 可能不会创建这个过滤器，限流不生效。  
- `GlobalFilter`：几乎所有进网关的请求都会经过 `filter`。  
- `Ordered`：提供 `getOrder`，控制多个过滤器的先后顺序。

### 7.2 字段含义

| 字段 | 作用 |
|------|------|
| `log` | 超限时 `log.warn(...)`，输出在 **gateway 控制台**，不是 Postman |
| `USER_GET_BY_ID` | 正则 `^/users/\\d+$`：匹配 `/users/1`，不匹配 `/users` |
| `LIMIT` | 阈值 3 |
| `WIDOW` | 窗口时长（秒）；Key 的过期时间 |
| `redisTemplate` | 执行 `increment` / `expire` |

正则符号简表：

| 符号 | 含义 |
|------|------|
| `^` | 从开头匹配 |
| `/users/` | 固定前缀 |
| `\\d+` | 一个或多个数字（Java 字符串里 `\` 要写成 `\\`） |
| `$` | 匹配到结尾 |

### 7.3 构造方法

```java
public IpRateLimitFilter(ReactiveStringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
}
```

- 名字必须与类名相同，否则 Java 不认作构造方法。  
- `this.redisTemplate`：当前对象字段。  
- 右边参数：Spring 传入的 Bean。  
- 目的：对象出生时就把 Redis 准备好（依赖注入）。

### 7.4 filter：取出请求信息

```java
ServerHttpRequest request = exchange.getRequest();
HttpMethod method = request.getMethod();
String path = request.getURI().getPath();
```

为什么从 `exchange` 取，而不是从 `chain` 取？

| 参数 | 像什么 | 有没有 path |
|------|--------|-------------|
| `exchange` | 本次请求的「包裹」（含 request/response） | **有** |
| `chain` | 「放行通道」 | **没有** |

`filter` 需要两个参数，是因为职责不同：

- `exchange`：读数据 / 写响应  
- `chain`：决定是否放行 `chain.filter(exchange)`

### 7.5 filter：非目标接口直接放行

```java
if (method != HttpMethod.GET || !USER_GET_BY_ID.matcher(path).matches()) {
    return chain.filter(exchange);
}
```

逻辑：

```text
不是 GET，或者路径不是 /users/数字
  → 不限流
  → return chain.filter(exchange)  // 放行并结束本方法
```

若不放行就结束，列表、创建等接口会被卡住或中断。

### 7.6 filter：拼 Redis Key

```java
String ip = resolveClientIp(request);
String redisKey = "rate:limit:" + ip + ":GET:" + path;
```

示例：`rate:limit:127.0.0.1:GET:/users/1`

设计意图：

- 不同 IP 分开计数  
- 不同路径分开计数（`/users/1` 与 `/users/2` 互不影响）

### 7.7 filter：计数、开窗口、判断（核心）

代码形态：

```java
return redisTemplate.opsForValue().increment(redisKey)
    .flatMap(count -> { /* 第一次则 expire */ })
    .flatMap(count -> { /* 超限则 429，否则放行 */ });
```

**同步思维版（帮助理解，不是网上运行的代码）：**

```text
count = INCR redisKey

如果 count == 1：
    EXPIRE redisKey，窗口秒数     // 只在第一次开窗口

如果 count > LIMIT：
    写 429
否则：
    放行
```

要点：

1. `increment` = Redis `INCR`，返回当前次数。  
2. **仅当 `count == 1` 时 `expire`**：开始窗口；第 2、3 次不要刷新过期，否则窗口会被拉长。  
3. `count > 3`：调用 `writeTooManyRequests`，**不要**再 `chain.filter`。  
4. 未超限：`chain.filter(exchange)` 转发到 user-service。

### 7.8 resolveClientIp

```text
1) 若有 X-Forwarded-For（代理场景）→ 取逗号前第一段并 trim
2) 否则用 remoteAddress 的 hostAddress（直连场景）
3) 都没有 → "unknown"（避免 Key 里出现 null）
```

基础语法注意：

- 先判 `xff != null` 再调 `isBlank()`，避免空指针。  
- `split(",")[0]`：先按逗号切成数组，再取下标 0。  
  错误写法示例：`split((",")[0]`（把字符串当数组用）。

### 7.9 writeTooManyRequests

```text
1) response.setStatusCode(429)
2) Content-Type = application/json
3) 文本块 """ ... """ 转 UTF-8 字节
4) wrap 成 DataBuffer
5) writeWith 写回客户端
```

注意：

- 参数类型必须是 **`ServerHttpResponse`**，不能是 Request。  
- 调用处应是 `exchange.getResponse()`，不是 `getRequest()`。  
- 文本块：`"""` 后面必须立刻换行，不能写成 `"""{"code":...`。  
- Postman 要看：**状态码 429** + Body 里的 `message`（文案是「过于频繁」，不一定含「繁忙」二字）。

### 7.10 getOrder

```java
return -100;
```

数字越小越先执行。由框架在组装过滤器链时调用。

---

## 8. 一次请求的完整执行时序

以 `GET http://localhost:8088/users/1` 为例：

```text
① Spring 启动时（只一次）
   new IpRateLimitFilter(redisTemplate)  → 构造方法

② 请求到达网关
   框架按 getOrder() 较先调用本过滤器

③ filter 开始
   取 method、path

④ 路径校验
   通过 → 继续；不通过 → chain.filter 结束

⑤ resolveClientIp → 拼 redisKey

⑥ Redis INCR → 得到 count
   count==1 → EXPIRE（打开窗口倒计时）

⑦ 分支
   count > 3 → writeTooManyRequests → 客户端收到 429
   否则      → chain.filter → 路由到 user-service → 常见 200

⑧ 窗口到期（如 10 秒后）
   Redis 删除 key → 下次请求从 1 重新计
```

次数示例（窗口未过期时）：

| 第几次 | count | 结果 |
|--------|-------|------|
| 1 | 1 | 放行 200 |
| 2 | 2 | 放行 200 |
| 3 | 3 | 放行 200 |
| 4 | 4 | 拦截 429 |
| 5 | 5 | 拦截 429 |

---

## 9. 构造方法 vs 普通方法（本类中的用法）

| | 构造方法 | 普通方法 |
|--|----------|----------|
| 本类 | `IpRateLimitFilter(...)` | `filter` / `resolveClientIp` / `writeTooManyRequests` / `getOrder` |
| 目的 | 注入 Redis，完成初始化 | 处理请求、算 IP、写 429、返回顺序 |
| 执行次数 | 进程生命周期内创建 Bean 时一次 | 每个请求可能多次进入 `filter` |

理解口诀（可直接背）：

> 构造方法给对象定好初始能力；  
> 普通方法基于当前状态多次执行行为。

---

## 10. 异步写法（Mono / flatMap）如何对应同步逻辑

网关不能像普通代码那样「调用完立刻拿到 int」，所以写成流水线。

| 异步代码 | 同步含义 |
|----------|----------|
| `increment(redisKey)` | `count = incr(key)` |
| 第一个 `flatMap` 里 `count==1 ? expire : just(true)` | `if (count==1) expire` |
| `thenReturn(count)` | 把 count 交给下一步 |
| 第二个 `flatMap` 里 `count > LIMIT` | `if (count > 3) 拒绝 else 放行` |

学习建议：先背同步版五步，再对照异步版「多出来的只是等待语法」。

---

## 11. 测试与排错

### 11.1 正确测试地址

```text
GET http://localhost:8088/users/1
```

| 错误测法 | 现象 |
|----------|------|
| 端口 8081 | 一直 200，绕过网关 |
| `/users?page=1` | 一直 200，正则不匹配 |
| `/users/1/`（多斜杠） | 可能不限流或异常 |
| 两次间隔超过窗口 | 每次都像第 1 次，看不到 429 |
| 只看 Postman 的 OK | OK 通常表示 200；429 时状态行是 Too Many Requests |

### 11.2 推荐验证命令（PowerShell）

```powershell
1..5 | ForEach-Object {
  curl.exe -s -o NUL -w "第$_次 status=%{http_code}`n" http://localhost:8088/users/1
}
```

期望：前几次 `200`，后面出现 `429`。

### 11.3 依赖与配置检查

- Docker Redis 运行中：`docker ps` 能看到 `6379`  
- 网关 `pom.xml` 含 `spring-boot-starter-data-redis-reactive`  
- `application.yml` 配置 `spring.data.redis.host/port`  
- 改代码后必须**重启 gateway**

### 11.4 曾出现过的典型语法错误（复盘用）

| 错误 | 原因 |
|------|------|
| import 非 reactive 的 `ServerHttpRequest` | 网关必须用 `...server.reactive...` |
| `writeTooManyRequests(exchange.getRequest())` | 应传 `getResponse()` |
| 方法参数写成 `ServerHttpRequest response` | 写响应必须用 `ServerHttpResponse` |
| `xff.split((",")[0].trim())` | 应写成 `xff.split(",")[0].trim()` |
| 文本块 `"""{` 同一行 | `"""` 后必须换行 |

---

## 12. 面试常问提炼

1. **限流为什么常用 Redis String + INCR + EXPIRE？**  
   原子计数 + TTL 做固定窗口，实现简单。

2. **为什么只在 count==1 时 EXPIRE？**  
   防止每次请求刷新 TTL 导致窗口被无限拉长。

3. **限流放网关的好处？**  
   统一入口、保护后端、与路由鉴权同层治理。

4. **缓存和限流都用 Redis，区别？**  
   缓存存业务数据提速；限流存计数控速。

5. **Gateway 为何用 Reactive Redis？**  
   Gateway 基于 WebFlux，阻塞调用会卡住事件循环。

6. **固定窗口的缺点？（进阶）**  
   窗口边界可能出现 2 倍突发；进阶可学滑动窗口、令牌桶。

---

## 13. 自测题与答案

### 题目

1. `exchange` 和 `chain` 分别负责什么？  
2. 为什么方法和路径要从 `exchange` 取？  
3. 构造方法和 `filter` 谁在创建时执行？谁在每次请求执行？  
4. Redis Key 为什么要拼上 IP 和 path？  
5. Postman 一直 200、curl 能 429，最常见原因是什么？  
6. `log.warn` 会出现在 Postman 里吗？  

### 参考答案

1. `exchange`：本次请求/响应数据；`chain`：放行到后续过滤器/路由。  
2. 请求数据在包裹里；`chain` 只是通道，没有 path。  
3. 构造：创建 Bean 时一次；`filter`：每个请求都可能进入。  
4. 按用户、按接口分别计数。  
5. Postman 打到了 8081 或非 getById 路径，或点击间隔超过窗口。  
6. 不会；只在 gateway 控制台。Postman 看 429 和 Body。

---

## 14. 复盘检查清单

每隔一段时间用此表自检：

- [ ] 能画出「客户端 → 网关 → 过滤器 → Redis → 放行/429」图  
- [ ] 能区分类型名 / 变量名 / 方法名  
- [ ] 能说出构造方法与普通方法的目的差异  
- [ ] 能解释为何从 `exchange` 取数、用 `chain` 放行  
- [ ] 能用同步五步解释 `increment + expire + 判断`  
- [ ] 知道必须访问 **8088** 且路径为 **`/users/{数字}`**  
- [ ] 知道部门缓存与 IP 限流都用 Redis 但职责不同  
- [ ] 能独立说出 429 时为什么不再调用 `chain.filter`

---

## 附录 A：相关文件清单

| 文件 | 作用 |
|------|------|
| `guo-gateway/.../filter/IpRateLimitFilter.java` | 限流过滤器实现 |
| `guo-gateway/pom.xml` | 含 gateway、nacos、reactive redis 依赖 |
| `guo-gateway/src/main/resources/application.yml` | 端口 8088、Redis、路由 |
| `guo-user-service/.../UserController.java` | 真正的 `getById` 业务接口 |

## 附录 B：配置片段参考（网关 Redis）

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:127.0.0.1}
      port: ${REDIS_PORT:6379}
      database: 0
      timeout: 3000ms
```

## 附录 C：启动 Redis（Docker）

```bat
docker start redis
```

若无容器：

```bat
docker run -d --name redis -p 6379:6379 redis:7
```

验证：

```bat
docker exec -it redis redis-cli ping
```

期望返回：`PONG`。

---

**文档结束。**  
建议复盘顺序：第 1～4 节建立大图 → 第 5 节补语法 → 第 7～8 节对照源码 → 第 13 节自测。

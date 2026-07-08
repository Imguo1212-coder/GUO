package com.test.guo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "Spring Boot + MyBatis 项目启动成功";
    }
    /**
     * 访问地址：http://localhost:8080/hello
     * - GET：请求方式（浏览器直接访问就是 GET）
     * - /hello：接口路径
     */
}

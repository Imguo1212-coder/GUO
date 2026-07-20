package com.test.guo.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;


@Component
public class IdempotencyFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyFilter.class);

    public static final String HEADER = "Idempotency-Key";
    private static final String KEY_PREFIX = "idempotency:";
    private static final Duration TTL = Duration.ofMinutes(1);

    private final ReactiveStringRedisTemplate redisTemplate;

    public IdempotencyFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpMethod method = exchange.getRequest().getMethod();
        String path = exchange.getRequest().getURI().getPath();

        // 只拦截「创建用户」
        if (method != HttpMethod.POST || !"/users".equals(path)) {
            return chain.filter(exchange);
        }

        String idemKey = exchange.getRequest().getHeaders().getFirst(HEADER);
        if (idemKey == null || idemKey.isBlank()) {
            return chain.filter(exchange);
        }

        String redisKey = KEY_PREFIX + "POST:/users:" + idemKey.trim();

        return redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "1", TTL)
                .flatMap(firstTime -> {
                    if (Boolean.TRUE.equals(firstTime)) {
                        return chain.filter(exchange);
                    }
                    log.warn("防重复拦截 path=/users key={}", idemKey);
                    return writeDuplicate(exchange);
                });
    }

    private Mono<Void> writeDuplicate(ServerWebExchange exchange) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.CONFLICT);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = """
                {"code":409,"message":"重复请求，请勿重复提交","data":null}
                """.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -90;
    }
}
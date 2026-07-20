package com.test.guo.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.core.io.buffer.DataBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Pattern;

//@Component
public class IpRateLimitFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(IpRateLimitFilter.class);
    private static final Pattern USER_GET_BY_ID=
            Pattern.compile("^/users/\\d+$");
    private static final int LIMIT = 3;
    private static final Duration WIDOW = Duration.ofSeconds(1);
    private final ReactiveStringRedisTemplate redisTemplate;

    public IpRateLimitFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    @Override
    public Mono<Void>filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        HttpMethod method = request.getMethod();
        String path = request.getURI().getPath();

        if (method != HttpMethod.GET || !USER_GET_BY_ID.matcher(path).matches()) {
            return chain.filter(exchange);
        }

        String ip = resolveClientIp(request);
        String redisKey = "rate:limit:" + ip + ":GET:" + path;

        return redisTemplate.opsForValue().increment(redisKey)
                .flatMap(count -> {
            Mono<Boolean> expireMono = (count != null && count == 1)
                    ? redisTemplate.expire(redisKey, WIDOW)
                    : Mono.just(true);

            return  expireMono.thenReturn(count == null ? 1L : count);})
                .flatMap(count ->{

            if (count>LIMIT){
                log.warn("限流拦截 ip={}，path={},count{}",ip,path,count);
                return writeTooManyRequests(exchange.getResponse());
            }
            return chain.filter(exchange);
        });
    }
    private String resolveClientIp(ServerHttpRequest request){
        String xff = request.getHeaders().getFirst("X-Forwarded-For");

        if (xff !=null && !xff.isBlank()){
            return xff.split(",")[0].trim();
        }

        if (request.getRemoteAddress()!=null
                &&request.getRemoteAddress().getAddress().getHostAddress()!=null){
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }
    private Mono<Void>writeTooManyRequests(ServerHttpResponse response){
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[]bytes = """
                {"code":429,"message":"请求过于频繁，请稍后再试","data":null}
                """.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}

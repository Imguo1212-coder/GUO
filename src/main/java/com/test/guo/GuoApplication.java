package com.test.guo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * Spring Boot 启动类
 * @MapperScan 的作用：告诉 MyBatis 去扫描 com.test.guo.mapper 包下的接口,
 * 扫描到后，Spring 才能创建 UserMapper 对象供 Service 使用
 */
@SpringBootApplication
@MapperScan("com.test.guo.mapper")
public class GuoApplication {
    public static void main(String[] args) {
        SpringApplication.run(GuoApplication.class, args);
    }
}
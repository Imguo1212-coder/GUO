package com.test.guo.dept;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.test.guo.dept")
@MapperScan("com.test.guo.dept.mapper")
@EnableDiscoveryClient
public class DeptServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeptServiceApplication.class, args);
    }
}

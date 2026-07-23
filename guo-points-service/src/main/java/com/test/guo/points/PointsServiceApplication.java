package com.test.guo.points;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.test.guo.points.mapper")
public class PointsServiceApplication {
    public static void main(String[] args){
        SpringApplication.run(
                PointsServiceApplication.class,
                args
        );
    }
}

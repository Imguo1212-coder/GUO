package com.test.guo.behavior;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class BehaviorServiceApplication {

    public static void main(String[] args){
        SpringApplication.run(BehaviorServiceApplication.class,args);
    }
}

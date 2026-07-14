package com.test.guo.user.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/config")
@RefreshScope
public class ConfigController {

    @Value("${demo.message:默认消息}")
    private String message;

    @Value("${demo.enabled:false}")
    private Boolean enabled;

    @Value("${demo.timeout:1000}")
    private Integer timeout;

    @GetMapping("/message")
    public String message() {
        return message;
    }

    @GetMapping("/info")
    public String info() {
        return "message=" + message
                + ", enabled=" + enabled
                + ", timeout=" + timeout;
    }
}

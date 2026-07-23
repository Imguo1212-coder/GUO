package com.test.guo.user.service;

import com.test.guo.common.event.UserCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class WelcomeService {

    private static final Logger log = LoggerFactory.getLogger(WelcomeService.class);

    public void handleUserCreated(UserCreatedEvent event) {
        log.info(
                "发送欢迎邮件给 {} <{}>",
                event.getName(),
                event.getEmail());

    }
}

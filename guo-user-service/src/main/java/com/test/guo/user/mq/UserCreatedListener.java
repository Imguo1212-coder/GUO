package com.test.guo.user.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.test.guo.user.config.KafkaConfig;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import com.test.guo.user.service.WelcomeService;
@Component
public class UserCreatedListener {
    private final WelcomeService  welcomeService;
    public UserCreatedListener(WelcomeService welcomeService){
        this.welcomeService = welcomeService;
    }

    @KafkaListener(
            topics = KafkaConfig.USER_CREATED_TOPIC,
            groupId = "user-service"
    )
    public void onUserCreated(UserCreatedEvent event){
        welcomeService.handleUserCreated(event);
    }
}

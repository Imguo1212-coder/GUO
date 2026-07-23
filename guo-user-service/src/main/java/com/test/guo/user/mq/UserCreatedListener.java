package com.test.guo.user.mq;

import com.test.guo.user.config.KafkaConfig;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.test.guo.user.service.WelcomeService;
import com.test.guo.common.event.UserCreatedEvent;
@Component
public class UserCreatedListener {
    private final WelcomeService  welcomeService;
    public UserCreatedListener(WelcomeService welcomeService){
        this.welcomeService = welcomeService;
    }

    @KafkaListener(
            topics = KafkaConfig.USER_CREATED_TOPIC,
            groupId = "welcome-service"
    )
    public void onUserCreated(UserCreatedEvent event){
        welcomeService.handleUserCreated(event);
    }
}

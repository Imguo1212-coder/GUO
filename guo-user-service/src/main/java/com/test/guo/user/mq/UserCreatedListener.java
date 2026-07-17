package com.test.guo.user.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.test.guo.user.config.KafkaConfig;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedListener {
    private static final Logger log = LoggerFactory.getLogger(UserCreatedListener.class);

    @KafkaListener(topics = KafkaConfig.USER_CREATED_TOPIC)
    public void onUserCreated(UserCreatedEvent event){

        log.info("收到用户创建消息，开始处理：{}",event);
    }
}

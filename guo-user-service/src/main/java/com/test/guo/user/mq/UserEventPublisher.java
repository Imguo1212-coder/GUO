package com.test.guo.user.mq;

import com.test.guo.user.config.KafkaConfig;
import com.test.guo.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(UserEventPublisher.class);
    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    public UserEventPublisher(KafkaTemplate<String, UserCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserCreated(User user) {
        UserCreatedEvent event = new UserCreatedEvent(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getDepartmentId(),
                user.getCreateTime()
        );
        kafkaTemplate.send(KafkaConfig.USER_CREATED_TOPIC, event);
        log.info("已发送用户创建信息：{}", event);
    }
}

package com.test.guo.user.mq;

import com.test.guo.user.config.RabbitConfig;
import com.test.guo.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;



@Component
public class UserEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(UserEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public UserEventPublisher(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
    }
    public void publishUserCreated(User user){
        UserCreatedEvent event = new UserCreatedEvent(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getDepartmentId(),
                user.getCreateTime()
        );
        rabbitTemplate.convertAndSend(RabbitConfig.USER_CREATED_QUEUE,event);
        log.info("已发送用户创建信息：{}",event);
    }
}

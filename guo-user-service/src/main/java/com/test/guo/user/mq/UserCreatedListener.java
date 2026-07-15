package com.test.guo.user.mq;

import com.test.guo.user.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedListener {
    private static final Logger log = LoggerFactory.getLogger(UserCreatedListener.class);

    @RabbitListener(queues = RabbitConfig.USER_CREATED_QUEUE)
    public void onUserCreated(UserCreatedEvent event){
        log.info("收到用户创建消息，开始处理：{}",event);
    }
}

package com.test.guo.user.mq;

import com.test.guo.common.event.KafkaTopics;
import com.test.guo.common.event.UserBehaviorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class UserBehaviorPublisher {
    private static final Logger log =
            LoggerFactory.getLogger(UserBehaviorPublisher.class);

    private final KafkaTemplate<String,UserBehaviorEvent> kafkaTemplate;

    public UserBehaviorPublisher(KafkaTemplate<String,UserBehaviorEvent> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(
            String behaviorType,
            Long targetUserId,
            Map<String, Object> details){

        UserBehaviorEvent event = new UserBehaviorEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setBehaviorType(behaviorType);
        event.setTargetUserId(targetUserId);
        event.setSource("user-service");
        event.setDetails(details);
        event.setOccurredAt(LocalDateTime.now());

        String key = targetUserId ==null
                ? event.getEventId()
                : targetUserId.toString();

        kafkaTemplate.send(
                KafkaTopics.USER_BEHAVIOR,
                key,
                event
        );
        log.info("已发送用户行为事件：{}",event);
    }
}

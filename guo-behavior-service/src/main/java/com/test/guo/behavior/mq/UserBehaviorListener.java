package com.test.guo.behavior.mq;

import com.test.guo.behavior.service.UserBehaviorService;
import com.test.guo.common.event.KafkaTopics;
import com.test.guo.common.event.UserBehaviorEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserBehaviorListener {
    private final UserBehaviorService userBehaviorService;

    public UserBehaviorListener(UserBehaviorService userBehaviorService){
        this.userBehaviorService = userBehaviorService;
    }
    @KafkaListener(
            topics = KafkaTopics.USER_BEHAVIOR,
            groupId = "behavior-service"
    )
    public void onUserBehavior(UserBehaviorEvent event) {
        userBehaviorService.save(event);
    }

}

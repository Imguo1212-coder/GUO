package com.test.guo.points.mq;


import com.test.guo.common.event.KafkaTopics;
import com.test.guo.common.event.UserCreatedEvent;
import com.test.guo.points.service.PointsService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedPointsListener {
    private final PointsService pointsService;

    public UserCreatedPointsListener(PointsService pointsService){
        this.pointsService = pointsService;
    }
    @KafkaListener(
            topics = KafkaTopics.USER_CREATED,
            groupId = "points-service"
    )
    public void onUserCreated(UserCreatedEvent event){
        pointsService.grantNewUserPoints(event);
    }
}

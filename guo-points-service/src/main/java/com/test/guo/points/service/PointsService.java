package com.test.guo.points.service;

import com.test.guo.common.event.UserCreatedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.test.guo.points.mapper.PointsAccountMapper;
import com.test.guo.points.mapper.PointsRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointsService {
    private static final Logger log =
            LoggerFactory.getLogger(PointsService.class);
    private static final int NEW_USER_POINTS = 100;

    private final PointsRecordMapper pointsRecordMapper;
    private final PointsAccountMapper pointsAccountMapper;

    public PointsService(
            PointsRecordMapper pointsRecordMapper,PointsAccountMapper pointsAccountMapper){
        this.pointsRecordMapper = pointsRecordMapper;
        this.pointsAccountMapper = pointsAccountMapper;
    }
    @Transactional
    public void grantNewUserPoints(UserCreatedEvent event){
        if (event.getEventId() == null || event.getEventId().isBlank()){
            throw new IllegalArgumentException("Kafka事件缺少eventId");
        }
        int inserted = pointsRecordMapper.insertRecord(
                event.getEventId(),
                event.getUserId(),
                NEW_USER_POINTS,
                "新用户注册奖励"
        );
        if(inserted == 0){
            log.info(
                    "事件已经处理，跳过重复发积分：{}",
                    event.getEventId(),
                    NEW_USER_POINTS
            );
            return;
        }
        pointsAccountMapper.addPoints(
                event.getUserId(),
                NEW_USER_POINTS
        );
        log.info(
                "用户积分发放成功，userId={}，points={}",
                event.getUserId(),
                NEW_USER_POINTS
        );
    }
}

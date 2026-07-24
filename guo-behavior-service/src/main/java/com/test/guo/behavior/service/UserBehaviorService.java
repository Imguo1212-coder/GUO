package com.test.guo.behavior.service;

import com.test.guo.behavior.document.UserBehaviorDocument;
import com.test.guo.behavior.repository.UserBehaviorRepository;
import com.test.guo.common.event.UserBehaviorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserBehaviorService {
    private static final Logger log =
            LoggerFactory.getLogger(UserBehaviorService.class);

    private final UserBehaviorRepository repository;

    public UserBehaviorService(UserBehaviorRepository repository) {
        this.repository = repository;
    }
    public boolean save(UserBehaviorEvent event) {
        if (event.getEventId() == null || event.getEventId().isBlank()) {
            throw new IllegalArgumentException("用户行为事件缺少eventId");
        }

        UserBehaviorDocument document = new UserBehaviorDocument();

        document.setEventId(event.getEventId());
        document.setBehaviorType(event.getBehaviorType());
        document.setTargetUserId(event.getTargetUserId());
        document.setSource(event.getSource());
        document.setDetails(event.getDetails());

        if (event.getOccurredAt() == null) {
            document.setOccurredAt(LocalDateTime.now());
        } else {
            document.setOccurredAt(event.getOccurredAt());
        }

        try{
            repository.save(document);

            log.info(
                    "用户行为保存成功，eventId={}，type={}，userId={}",
                    event.getEventId(),
                    event.getBehaviorType(),
                    event.getTargetUserId()
            );
            return true;
        } catch (DuplicateKeyException exception){
            log.info(
                    "用户行为已经保存，跳过重复事件，eventId={}",
                    event.getEventId()
            );
            return false;
        }

    }
}

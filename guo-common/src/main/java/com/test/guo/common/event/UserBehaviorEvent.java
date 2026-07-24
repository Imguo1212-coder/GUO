package com.test.guo.common.event;

import java.time.LocalDateTime;
import java.util.Map;

public class UserBehaviorEvent {
    private String eventId;
    private String behaviorType;
    private Long targetUserId;  //被操作的用户ID
    private String source;  //  事件来源
    private Map<String,Object> details;
    private LocalDateTime occurredAt;

    public UserBehaviorEvent(){

    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getBehaviorType() {
        return behaviorType;
    }

    public void setBehaviorType(String behaviorType) {
        this.behaviorType = behaviorType;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    @Override
    public String toString() {
        return "UserBehaviorEvent{" +
                "eventId='" + eventId + '\'' +
                ", behaviorType='" + behaviorType + '\'' +
                ", targetUserId=" + targetUserId +
                ", source='" + source + '\'' +
                ", details=" + details +
                ", occurredAt=" + occurredAt +
                '}';
    }
}

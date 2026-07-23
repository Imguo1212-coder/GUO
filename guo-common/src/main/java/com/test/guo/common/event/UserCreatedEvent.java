package com.test.guo.common.event;

import java.time.LocalDateTime;

public class UserCreatedEvent {
    private String eventId;
    private Long userId;
    private  String name;
    private String email;
    private Long departmentId;
    private LocalDateTime createTime;
    private LocalDateTime eventTime;

    public UserCreatedEvent(){

    }
    public UserCreatedEvent(
            String eventId,
            Long userId,
            String name,
            String email,
            Long departmentId,
            LocalDateTime createTime,
            LocalDateTime eventTime){

        this.eventId = eventId;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.departmentId = departmentId;
        this.createTime = createTime;
        this.eventTime = eventTime;
    }

    public String getEventId() {
        return eventId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    @Override
    public String toString() {
        return "UserCreatedEvent{" +
                "eventId='" + eventId + '\'' +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", departmentId=" + departmentId +
                ", createTime=" + createTime +
                ", eventTime=" + eventTime +
                '}';
    }
}

package com.test.guo.user.mq;

import java.time.LocalDateTime;

public class UserCreatedEvent {
    private Long userId;
    private String name;
    private String email;
    private Long departmentId;
    private LocalDateTime createTime;

    public UserCreatedEvent() {
    }

    public UserCreatedEvent(Long userId, String name, String email, Long departmentId, LocalDateTime createTime) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.departmentId = departmentId;
        this.createTime = createTime;
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

    public void setUserId(Long userId) {
        this.userId = userId;
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

    @Override
    public String toString() {
        return "UserCreatedEvent{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", departmentId=" + departmentId +
                ", createTime=" + createTime +
                '}';
    }
}

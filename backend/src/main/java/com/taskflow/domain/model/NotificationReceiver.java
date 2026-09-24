package com.taskflow.domain.model;

import java.time.Instant;

public class NotificationReceiver {
    private Long id;
    private Long notificationId;
    private Long userId;
    private Boolean isRead;
    private Instant readAt;

    public NotificationReceiver(Long id, Long notificationId, Long userId, Boolean isRead, Instant readAt) {
        this.id = id;
        this.notificationId = notificationId;
        this.userId = userId;
        this.isRead = isRead;
        this.readAt = readAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getNotificationId() { return notificationId; }
    public Long getUserId() { return userId; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }
}

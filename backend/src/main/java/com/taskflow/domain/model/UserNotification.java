package com.taskflow.domain.model;

import java.time.Instant;

public class UserNotification {
    private Long id; // Receiver ID
    private Long userId;
    private String type;
    private String title;
    private String message;
    private Boolean read;
    private Instant createdAt;

    public UserNotification(Long id, Long userId, String type, String title, String message, Boolean read, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public Boolean getRead() { return read; }
    public Instant getCreatedAt() { return createdAt; }
}

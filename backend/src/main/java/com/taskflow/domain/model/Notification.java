package com.taskflow.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Notification {
    private Long id;
    private String type;
    private String title;
    private String message;
    private UUID relatedTaskId;
    private Instant createdAt;
    private Instant expiresAt;

    public Notification(Long id, String type, String title, String message, UUID relatedTaskId, Instant createdAt, Instant expiresAt) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedTaskId = relatedTaskId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public UUID getRelatedTaskId() { return relatedTaskId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
}

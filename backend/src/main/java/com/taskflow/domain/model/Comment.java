package com.taskflow.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Comment {
    private UUID id;
    private Long userId;
    private String content;
    private Instant createdAt;

    public Comment() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }

    public Comment(Long userId, String content) {
        this();
        this.userId = userId;
        this.content = content;
    }

    // getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

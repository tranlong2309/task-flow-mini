package com.taskflow.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Subtask {
    private UUID id;
    private String title;
    private boolean isCompleted;
    private Instant createdAt;

    public Subtask() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }

    public Subtask(String title) {
        this();
        this.title = title;
        this.isCompleted = false;
    }

    // getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(boolean isCompleted) { this.isCompleted = isCompleted; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

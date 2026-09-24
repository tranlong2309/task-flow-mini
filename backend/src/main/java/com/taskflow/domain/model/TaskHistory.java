package com.taskflow.domain.model;

import java.time.Instant;
import java.util.UUID;

public class TaskHistory {
    private Long id;
    private UUID taskId;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private Long changedBy;
    private Instant changedAt;

    public TaskHistory(Long id, UUID taskId, String fieldName, String oldValue, String newValue, Long changedBy, Instant changedAt) {
        this.id = id;
        this.taskId = taskId;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
    }

    public Long getId() { return id; }
    public UUID getTaskId() { return taskId; }
    public String getFieldName() { return fieldName; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public Long getChangedBy() { return changedBy; }
    public Instant getChangedAt() { return changedAt; }
}

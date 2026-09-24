package com.taskflow.domain.model;

import java.time.Instant;
import java.util.UUID;

public class TaskSearchResult {
    private UUID id;
    private String title;
    private String status;
    private Long assigneeId;
    private Priority priority;
    private Instant dueDate;

    public TaskSearchResult(UUID id, String title, String status, Long assigneeId, Priority priority, Instant dueDate) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.assigneeId = assigneeId;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public Long getAssigneeId() { return assigneeId; }
    public Priority getPriority() { return priority; }
    public Instant getDueDate() { return dueDate; }
}

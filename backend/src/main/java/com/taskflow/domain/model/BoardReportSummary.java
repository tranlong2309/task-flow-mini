package com.taskflow.domain.model;

import java.time.Instant;
import java.util.UUID;

public class BoardReportSummary {
    private UUID boardId;
    private Instant generatedAt;
    private long totalTasks;
    private long done;
    private long inProgress;
    private long blocked;
    private long overdue;
    private double completionRate;

    public BoardReportSummary(UUID boardId, Instant generatedAt, long totalTasks, long done, long inProgress, long blocked, long overdue, double completionRate) {
        this.boardId = boardId;
        this.generatedAt = generatedAt;
        this.totalTasks = totalTasks;
        this.done = done;
        this.inProgress = inProgress;
        this.blocked = blocked;
        this.overdue = overdue;
        this.completionRate = completionRate;
    }

    public UUID getBoardId() { return boardId; }
    public Instant getGeneratedAt() { return generatedAt; }
    public long getTotalTasks() { return totalTasks; }
    public long getDone() { return done; }
    public long getInProgress() { return inProgress; }
    public long getBlocked() { return blocked; }
    public long getOverdue() { return overdue; }
    public double getCompletionRate() { return completionRate; }
}

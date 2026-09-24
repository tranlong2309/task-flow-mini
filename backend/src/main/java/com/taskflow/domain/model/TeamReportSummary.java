package com.taskflow.domain.model;

import java.util.List;

public class TeamReportSummary {
    private Long teamId;
    private String from;
    private String to;
    private long totalTasks;
    private long done;
    private long blocked;
    private long overdue;
    private double completionRate;
    private List<Task> tasks; // List of tasks matching the criteria

    public TeamReportSummary(Long teamId, String from, String to, long totalTasks, long done, long blocked, long overdue, double completionRate, List<Task> tasks) {
        this.teamId = teamId;
        this.from = from;
        this.to = to;
        this.totalTasks = totalTasks;
        this.done = done;
        this.blocked = blocked;
        this.overdue = overdue;
        this.completionRate = completionRate;
        this.tasks = tasks;
    }

    public Long getTeamId() { return teamId; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public long getTotalTasks() { return totalTasks; }
    public long getDone() { return done; }
    public long getBlocked() { return blocked; }
    public long getOverdue() { return overdue; }
    public double getCompletionRate() { return completionRate; }
    public List<Task> getTasks() { return tasks; }
}

package com.taskflow.domain.model;

public class AssigneeWorkload {
    private Long assigneeId;
    private String fullName;
    private long activeTasks;
    private long overdueTasks;
    private long blockedTasks;

    public AssigneeWorkload(Long assigneeId, String fullName, long activeTasks, long overdueTasks, long blockedTasks) {
        this.assigneeId = assigneeId;
        this.fullName = fullName;
        this.activeTasks = activeTasks;
        this.overdueTasks = overdueTasks;
        this.blockedTasks = blockedTasks;
    }

    public Long getAssigneeId() { return assigneeId; }
    public String getFullName() { return fullName; }
    public long getActiveTasks() { return activeTasks; }
    public long getOverdueTasks() { return overdueTasks; }
    public long getBlockedTasks() { return blockedTasks; }
}

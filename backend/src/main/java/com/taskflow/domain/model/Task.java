package com.taskflow.domain.model;

import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class Task {
    private UUID id;
    private UUID boardId;
    private String title;
    private String description;
    private Long statusColumnId;
    private Set<Long> assigneeIds = new HashSet<>();
    private Instant assignedDate;
    private Instant startDate;
    private List<Subtask> subtasks = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();
    private List<Attachment> attachments = new ArrayList<>();
    private Priority priority;
    private Instant dueDate;
    private Long createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private Integer position;
    private Instant completedAt;
    private Boolean isBlocked;
    private String blockedReason;
    private Instant blockedAt;

    public Task() {}

    public Task(UUID id, UUID boardId, String title, String description, Long statusColumnId, Set<Long> assigneeIds, Instant assignedDate, Instant startDate, List<Subtask> subtasks, List<Comment> comments, List<Attachment> attachments, Priority priority, Instant dueDate, Long createdBy, Instant createdAt, Instant updatedAt, Instant deletedAt, Integer position, Instant completedAt, Boolean isBlocked, String blockedReason, Instant blockedAt) {
        this.id = id;
        this.boardId = boardId;
        this.title = title;
        this.description = description;
        this.statusColumnId = statusColumnId;
        this.assigneeIds = assigneeIds != null ? assigneeIds : new HashSet<>();
        this.assignedDate = assignedDate;
        this.startDate = startDate;
        this.subtasks = subtasks != null ? subtasks : new ArrayList<>();
        this.comments = comments != null ? comments : new ArrayList<>();
        this.attachments = attachments != null ? attachments : new ArrayList<>();
        this.priority = priority;
        this.dueDate = dueDate;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.position = position;
        this.completedAt = completedAt;
        this.isBlocked = isBlocked;
        this.blockedReason = blockedReason;
        this.blockedAt = blockedAt;
    }

    public Task(UUID id, UUID boardId, String title, String description, Long statusColumnId, Long assigneeId, Priority priority, Instant dueDate, Long createdBy, Instant createdAt, Instant updatedAt, Instant deletedAt, Integer position, Instant completedAt, Boolean isBlocked, String blockedReason, Instant blockedAt) {
        this(id, boardId, title, description, statusColumnId, 
             assigneeId != null ? new HashSet<>(List.of(assigneeId)) : new HashSet<>(), 
             null, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 
             priority, dueDate, createdBy, createdAt, updatedAt, deletedAt, position, 
             completedAt, isBlocked, blockedReason, blockedAt);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getBoardId() { return boardId; }
    public void setBoardId(UUID boardId) { this.boardId = boardId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getStatusColumnId() { return statusColumnId; }
    public void setStatusColumnId(Long statusColumnId) { this.statusColumnId = statusColumnId; }
    public Set<Long> getAssigneeIds() { return assigneeIds; }
    public void setAssigneeIds(Set<Long> assigneeIds) { this.assigneeIds = assigneeIds; }
    public Instant getAssignedDate() { return assignedDate; }
    public void setAssignedDate(Instant assignedDate) { this.assignedDate = assignedDate; }
    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }
    public List<Subtask> getSubtasks() { return subtasks; }
    public void setSubtasks(List<Subtask> subtasks) { this.subtasks = subtasks; }
    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }
    public List<Attachment> getAttachments() { return attachments; }
    public void setAttachments(List<Attachment> attachments) { this.attachments = attachments; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public Instant getDueDate() { return dueDate; }
    public void setDueDate(Instant dueDate) { this.dueDate = dueDate; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Boolean getIsBlocked() { return isBlocked; }
    public void setIsBlocked(Boolean isBlocked) { this.isBlocked = isBlocked; }
    public String getBlockedReason() { return blockedReason; }
    public void setBlockedReason(String blockedReason) { this.blockedReason = blockedReason; }
    public Instant getBlockedAt() { return blockedAt; }
    public void setBlockedAt(Instant blockedAt) { this.blockedAt = blockedAt; }
}

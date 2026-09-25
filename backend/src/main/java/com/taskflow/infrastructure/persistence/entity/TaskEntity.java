package com.taskflow.infrastructure.persistence.entity;

import com.taskflow.domain.model.Priority;
import com.taskflow.domain.model.Subtask;
import com.taskflow.domain.model.Comment;
import com.taskflow.domain.model.Attachment;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "tasks")
public class TaskEntity {
    @Id
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.BINARY)
    private UUID id;

    @Column(name = "board_id", nullable = false)
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.BINARY)
    private UUID boardId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "status_column_id")
    private Long statusColumnId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "assignee_ids", columnDefinition = "json")
    private Set<Long> assigneeIds = new HashSet<>();

    @Column(name = "assigned_date")
    private Instant assignedDate;

    @Column(name = "start_date")
    private Instant startDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "subtasks", columnDefinition = "json")
    private List<Subtask> subtasks = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "comments", columnDefinition = "json")
    private List<Comment> comments = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attachments", columnDefinition = "json")
    private List<Attachment> attachments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Column(name = "due_date", nullable = false)
    private Instant dueDate;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(nullable = false)
    private Integer position = 0;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "is_blocked", nullable = false)
    private Boolean isBlocked = false;

    @Column(name = "blocked_reason", columnDefinition = "TEXT")
    private String blockedReason;

    @Column(name = "blocked_at")
    private Instant blockedAt;

    public TaskEntity() {}

    // Getters and Setters
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

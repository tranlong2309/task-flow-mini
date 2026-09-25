package com.taskflow.infrastructure.web.dto;

import com.taskflow.domain.model.Priority;
import com.taskflow.domain.model.Task;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO cho danh sách Work Order — khớp với schema WorkOrderSummary trong docs/api-spec.yaml.
 */
public record WorkOrderSummary(
        UUID id,
        String title,
        Long statusColumnId,
        String statusColumnName,
        Priority priority,
        Instant dueDate,
        Long assigneeId,
        java.util.Set<Long> assigneeIds,
        boolean isBlocked,
        boolean isOverdue,
        Instant createdAt
) {
    public static WorkOrderSummary from(Task task, String columnName) {
        Long primaryAssigneeId = (task.getAssigneeIds() != null && !task.getAssigneeIds().isEmpty())
                ? task.getAssigneeIds().iterator().next()
                : null;

        boolean isOverdue = task.getCompletedAt() == null
                && task.getDueDate() != null
                && task.getDueDate().isBefore(Instant.now());

        return new WorkOrderSummary(
                task.getId(),
                task.getTitle(),
                task.getStatusColumnId(),
                columnName,
                task.getPriority(),
                task.getDueDate(),
                primaryAssigneeId,
                task.getAssigneeIds(),
                Boolean.TRUE.equals(task.getIsBlocked()),
                isOverdue,
                task.getCreatedAt()
        );
    }
}

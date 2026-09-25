package com.taskflow.infrastructure.web.dto;

import com.taskflow.domain.model.Priority;
import com.taskflow.domain.model.Task;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO cho Work Order response — khớp với schema WorkOrderResponse trong docs/api-spec.yaml.
 *
 * Spec reference: docs/api-spec.yaml → components/schemas/WorkOrderResponse
 * Domain reference: docs/domain-model.md → Aggregate Root: WorkOrder
 *
 * Lý do tồn tại: KHÔNG trả Task entity trực tiếp ra API.
 * Entity chứa toàn bộ trạng thái domain, một số field không nên expose ra ngoài.
 */
public record WorkOrderResponse(
        UUID id,
        UUID boardId,
        String title,
        String description,

        // Spec: status là derived từ statusColumnId (domain-model.md §4.5)
        Long statusColumnId,
        String statusColumnName,   // tên cột hiển thị trên board

        Priority priority,
        Instant dueDate,
        boolean isOverdue,         // server tính: !DONE && dueDate < now()

        // Assignee — spec dùng single assigneeId (OQ-06 chưa resolved, hiện tại lấy primary)
        Long assigneeId,

        boolean isBlocked,
        String blockedReason,      // nullable — chỉ có khi isBlocked=true
        Instant blockedAt,

        Instant completedAt,       // null nếu chưa Done; set khi vào cột Done

        Long createdBy,
        Instant createdAt,
        Instant updatedAt
) {

    /**
     * Factory method: map từ Task entity + tên cột sang DTO.
     * Đặt ở đây để tránh coupling domain với presentation.
     *
     * @param task          Task entity từ domain
     * @param columnName    Tên cột hiển thị (lấy từ BoardColumn)
     * @return WorkOrderResponse DTO
     */
    public static WorkOrderResponse from(Task task, String columnName) {
        // Lấy primary assigneeId (first element) — OQ-06: hiện tại multi-assignee nhưng spec là single
        Long primaryAssigneeId = (task.getAssigneeIds() != null && !task.getAssigneeIds().isEmpty())
                ? task.getAssigneeIds().iterator().next()
                : null;

        // Tính isOverdue: chưa completed và đã quá dueDate
        boolean isOverdue = task.getCompletedAt() == null
                && task.getDueDate() != null
                && task.getDueDate().isBefore(Instant.now());

        return new WorkOrderResponse(
                task.getId(),
                task.getBoardId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatusColumnId(),
                columnName,
                task.getPriority(),
                task.getDueDate(),
                isOverdue,
                primaryAssigneeId,
                Boolean.TRUE.equals(task.getIsBlocked()),
                task.getBlockedReason(),
                task.getBlockedAt(),
                task.getCompletedAt(),
                task.getCreatedBy(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    /**
     * Overload không có columnName — dùng khi không cần resolve tên cột.
     */
    public static WorkOrderResponse from(Task task) {
        return from(task, null);
    }
}

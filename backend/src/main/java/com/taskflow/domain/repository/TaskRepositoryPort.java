package com.taskflow.domain.repository;

import com.taskflow.infrastructure.web.dto.PagedResponse;

import com.taskflow.domain.model.Priority;
import com.taskflow.domain.model.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

public interface TaskRepositoryPort {
    Task save(Task task);
    java.util.List<Task> findTasksForReminders(java.time.Instant upTo);
    void saveAll(List<Task> tasks);
    Optional<Task> findById(UUID id);
    List<Task> findByStatusColumnIdOrderByPositionAsc(Long statusColumnId);
        PagedResponse<Task> searchTasks(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly, int page, int size);
    List<Task> searchTasksByBoardIds(List<UUID> boardIds, Long assigneeId, Instant from, Instant to);
    long countByBoardId(UUID boardId);
    long countByBoardIdAndCompletedAtIsNotNull(UUID boardId);
    long countByBoardIdAndStatusColumnId(UUID boardId, Long statusColumnId);
    long countByBoardIdAndIsBlockedTrue(UUID boardId);
    long countByBoardIdAndDueDateBeforeAndCompletedAtIsNull(UUID boardId, Instant date);
}

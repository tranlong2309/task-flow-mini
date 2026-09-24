package com.taskflow.domain.repository;

import com.taskflow.domain.model.Priority;
import com.taskflow.domain.model.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepositoryPort {
    Task save(Task task);
    Optional<Task> findById(UUID id);
    List<Task> searchTasks(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly);
}

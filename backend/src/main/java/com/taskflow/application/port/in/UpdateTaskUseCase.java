package com.taskflow.application.port.in;

import com.taskflow.domain.model.Priority;
import com.taskflow.domain.model.Task;

import java.time.Instant;
import java.util.UUID;

public interface UpdateTaskUseCase {
    Task updateTask(UUID taskId, String title, String description, Long assigneeId, Priority priority, Instant dueDate, Long statusColumnId, Long updaterId);
}

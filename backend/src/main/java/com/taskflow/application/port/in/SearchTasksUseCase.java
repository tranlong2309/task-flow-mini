package com.taskflow.application.port.in;

import com.taskflow.infrastructure.web.dto.PagedResponse;

import com.taskflow.domain.model.Priority;
import com.taskflow.domain.model.Task;
import java.util.List;
import java.util.UUID;

public interface SearchTasksUseCase {
        PagedResponse<Task> searchTasks(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly, Long requesterId, int page, int size);
}

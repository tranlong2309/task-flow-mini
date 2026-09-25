package com.taskflow.application.port.in;

import com.taskflow.infrastructure.web.dto.PagedResponse;

import com.taskflow.domain.model.Priority;
import com.taskflow.domain.model.Task;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

public interface SearchTasksUseCase {
        PagedResponse<Task> searchTasks(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly, 
                                        Instant assignedDateFrom, Instant assignedDateTo, 
                                        Instant startDateFrom, Instant startDateTo, 
                                        Instant endDateFrom, Instant endDateTo, 
                                        Long requesterId, int page, int size);
}

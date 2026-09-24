package com.taskflow.application.port.in;

import com.taskflow.domain.model.Task;
import java.util.UUID;

public interface UpdateTaskStatusUseCase {
    Task updateTaskStatus(UUID taskId, Long statusColumnId, String note, Long updaterId);
}

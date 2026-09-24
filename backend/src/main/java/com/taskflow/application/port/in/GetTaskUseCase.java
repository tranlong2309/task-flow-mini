package com.taskflow.application.port.in;

import com.taskflow.domain.model.Task;
import java.util.UUID;

public interface GetTaskUseCase {
    Task getTask(UUID taskId, Long requesterId);
}

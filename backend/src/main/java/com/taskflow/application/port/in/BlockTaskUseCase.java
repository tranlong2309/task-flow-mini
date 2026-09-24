package com.taskflow.application.port.in;

import com.taskflow.domain.model.Task;
import java.util.UUID;

public interface BlockTaskUseCase {
    Task blockTask(UUID taskId, String reason, Long updaterId);
}

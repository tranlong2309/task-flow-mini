package com.taskflow.application.port.in;

import com.taskflow.domain.model.Task;
import java.util.UUID;

public interface UnblockTaskUseCase {
    Task unblockTask(UUID taskId, Long updaterId);
}

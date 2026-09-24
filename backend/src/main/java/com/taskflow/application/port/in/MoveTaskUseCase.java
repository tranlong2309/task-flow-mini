package com.taskflow.application.port.in;

import com.taskflow.domain.model.Task;
import java.util.UUID;

public interface MoveTaskUseCase {
    Task moveTask(UUID taskId, Long sourceColumnId, Long targetColumnId, int sourceIndex, int targetIndex, Long updaterId);
}

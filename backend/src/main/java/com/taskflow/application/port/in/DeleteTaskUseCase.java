package com.taskflow.application.port.in;

import java.util.UUID;

public interface DeleteTaskUseCase {
    void deleteTask(UUID taskId, Long requesterId);
}

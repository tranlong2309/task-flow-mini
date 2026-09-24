package com.taskflow.application.port.in;

import com.taskflow.domain.model.BoardWorkloadSummary;

import java.util.UUID;

public interface GetWorkloadUseCase {
    BoardWorkloadSummary getWorkload(UUID boardId, Long userId);
}

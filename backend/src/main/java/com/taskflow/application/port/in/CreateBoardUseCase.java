package com.taskflow.application.port.in;

import com.taskflow.domain.model.Board;

public interface CreateBoardUseCase {
    Board createBoard(String name, String description, Long teamId, Long creatorId);
}

package com.taskflow.application.port.in;

import com.taskflow.domain.model.Board;
import java.util.UUID;

public interface UpdateBoardUseCase {
    Board updateBoard(UUID boardId, String name, String description);
}

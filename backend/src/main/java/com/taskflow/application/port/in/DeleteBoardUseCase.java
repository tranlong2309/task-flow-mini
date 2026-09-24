package com.taskflow.application.port.in;

import java.util.UUID;

public interface DeleteBoardUseCase {
    void deleteBoard(UUID boardId);
}

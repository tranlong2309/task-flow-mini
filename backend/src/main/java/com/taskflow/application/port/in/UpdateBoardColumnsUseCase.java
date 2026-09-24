package com.taskflow.application.port.in;

import com.taskflow.domain.model.BoardColumn;
import java.util.List;
import java.util.UUID;

public interface UpdateBoardColumnsUseCase {
    List<BoardColumn> updateBoardColumns(UUID boardId, List<BoardColumn> columns);
}

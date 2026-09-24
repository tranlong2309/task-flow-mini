package com.taskflow.application.port.in;

import com.taskflow.domain.model.Board;
import com.taskflow.domain.model.BoardColumn;
import java.util.List;
import java.util.UUID;

public interface GetBoardUseCase {
    Board getBoard(UUID boardId);
    List<BoardColumn> getBoardColumns(UUID boardId);
    List<Board> getBoardsByTeamId(Long teamId);
}

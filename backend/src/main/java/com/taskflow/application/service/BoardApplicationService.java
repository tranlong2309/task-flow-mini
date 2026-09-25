package com.taskflow.application.service;

import com.taskflow.application.port.in.CreateBoardUseCase;
import com.taskflow.domain.model.Board;
import com.taskflow.domain.model.BoardColumn;
import com.taskflow.domain.repository.BoardColumnRepositoryPort;
import com.taskflow.domain.repository.BoardRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BoardApplicationService implements CreateBoardUseCase, com.taskflow.application.port.in.GetBoardUseCase, com.taskflow.application.port.in.UpdateBoardUseCase, com.taskflow.application.port.in.DeleteBoardUseCase, com.taskflow.application.port.in.UpdateBoardColumnsUseCase {

    private final BoardRepositoryPort boardRepositoryPort;
    private final BoardColumnRepositoryPort boardColumnRepositoryPort;
    private final com.taskflow.domain.repository.BoardMemberRepositoryPort boardMemberRepositoryPort;

    public BoardApplicationService(BoardRepositoryPort boardRepositoryPort, 
                                   BoardColumnRepositoryPort boardColumnRepositoryPort,
                                   com.taskflow.domain.repository.BoardMemberRepositoryPort boardMemberRepositoryPort) {
        this.boardRepositoryPort = boardRepositoryPort;
        this.boardColumnRepositoryPort = boardColumnRepositoryPort;
        this.boardMemberRepositoryPort = boardMemberRepositoryPort;
    }

    @Override
    public Board createBoard(String name, String description, Long teamId, Long creatorId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Board name is required");
        }
        if (teamId == null) {
            throw new IllegalArgumentException("teamId is required");
        }

        Board board = new Board(UUID.randomUUID(), name, description, teamId);
        board = boardRepositoryPort.save(board);
        
        boardMemberRepositoryPort.save(board.getId(), creatorId, "MANAGER");
        
        return board;
    }

    @Override
    public Board getBoard(UUID boardId) {
        return boardRepositoryPort.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board not found"));
    }

    @Override
    public List<BoardColumn> getBoardColumns(UUID boardId) {
        return boardColumnRepositoryPort.findByBoardId(boardId);
    }

    @Override
    public List<Board> getBoardsByTeamId(Long teamId) {
        return boardRepositoryPort.findByTeamId(teamId);
    }

    @Override
    public Board updateBoard(UUID boardId, String name, String description) {
        Board board = boardRepositoryPort.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board not found"));
        if (name != null && !name.isBlank()) {
            board.setName(name);
        }
        if (description != null) {
            board.setDescription(description);
        }
        return boardRepositoryPort.save(board);
    }

    @Override
    public void deleteBoard(UUID boardId) {
        boardRepositoryPort.deleteById(boardId);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public List<BoardColumn> updateBoardColumns(UUID boardId, List<BoardColumn> columns) {
        boardColumnRepositoryPort.deleteAllByBoardId(boardId);
        List<BoardColumn> toSave = columns.stream().map(c -> new BoardColumn(null, boardId, c.getName(), c.getPosition(), c.getTone())).collect(java.util.stream.Collectors.toList());
        return boardColumnRepositoryPort.saveAll(toSave);
    }
}

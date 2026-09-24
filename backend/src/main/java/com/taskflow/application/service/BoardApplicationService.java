package com.taskflow.application.service;

import com.taskflow.application.port.in.CreateBoardUseCase;
import com.taskflow.domain.model.Board;
import com.taskflow.domain.repository.BoardRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BoardApplicationService implements CreateBoardUseCase {

    private final BoardRepositoryPort boardRepositoryPort;

    public BoardApplicationService(BoardRepositoryPort boardRepositoryPort) {
        this.boardRepositoryPort = boardRepositoryPort;
    }

    @Override
    public Board createBoard(String name, String description, Long teamId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Board name is required");
        }
        if (teamId == null) {
            throw new IllegalArgumentException("teamId is required");
        }

        Board board = new Board(UUID.randomUUID(), name, description, teamId);
        return boardRepositoryPort.save(board);
    }
}

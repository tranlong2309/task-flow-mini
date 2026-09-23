package com.taskflow.infrastructure.persistence.repository;

import com.taskflow.domain.model.Board;
import com.taskflow.domain.repository.BoardRepositoryPort;
import com.taskflow.infrastructure.persistence.entity.BoardEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class BoardRepositoryAdapter implements BoardRepositoryPort {

    private final SpringDataBoardRepository springDataBoardRepository;

    public BoardRepositoryAdapter(SpringDataBoardRepository springDataBoardRepository) {
        this.springDataBoardRepository = springDataBoardRepository;
    }

    @Override
    public Board save(Board board) {
        BoardEntity entity = toEntity(board);
        BoardEntity saved = springDataBoardRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Board> findById(UUID id) {
        return springDataBoardRepository.findById(id)
                .map(this::toDomain);
    }

    private BoardEntity toEntity(Board board) {
        return new BoardEntity(
                board.getId(),
                board.getName(),
                board.getDescription(),
                board.getTeamId(),
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }

    private Board toDomain(BoardEntity entity) {
        Board board = new Board(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getTeamId()
        );
        return board;
    }
}

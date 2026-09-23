package com.taskflow.domain.repository;

import com.taskflow.domain.model.Board;

import java.util.Optional;
import java.util.UUID;

public interface BoardRepositoryPort {
    Board save(Board board);
    Optional<Board> findById(UUID id);
}

package com.taskflow.domain.repository;

import com.taskflow.domain.model.BoardColumn;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardColumnRepositoryPort {
    Optional<BoardColumn> findById(Long id);
    List<BoardColumn> findByBoardId(UUID boardId);
}

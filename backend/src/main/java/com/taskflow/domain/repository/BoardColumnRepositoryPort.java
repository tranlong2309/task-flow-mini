package com.taskflow.domain.repository;

import com.taskflow.domain.model.BoardColumn;
import java.util.Optional;

public interface BoardColumnRepositoryPort {
    Optional<BoardColumn> findById(Long id);
}

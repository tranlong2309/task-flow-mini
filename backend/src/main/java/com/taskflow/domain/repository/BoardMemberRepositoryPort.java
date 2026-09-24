package com.taskflow.domain.repository;

import java.util.Optional;
import java.util.UUID;

public interface BoardMemberRepositoryPort {
    Optional<String> getRoleInBoard(UUID boardId, Long userId);
}

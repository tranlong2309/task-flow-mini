package com.taskflow.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardMemberRepositoryPort {
    Optional<String> getRoleInBoard(UUID boardId, Long userId);
    List<Long> findManagers(UUID boardId);
    void save(UUID boardId, Long userId, String roleName);
}

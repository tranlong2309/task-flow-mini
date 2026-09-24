package com.taskflow.infrastructure.persistence.repository;

import com.taskflow.infrastructure.persistence.entity.BoardMemberEntity;
import com.taskflow.infrastructure.persistence.entity.BoardMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataBoardMemberRepository extends JpaRepository<BoardMemberEntity, BoardMemberId> {
    Optional<BoardMemberEntity> findByBoardIdAndUserId(UUID boardId, Long userId);
}

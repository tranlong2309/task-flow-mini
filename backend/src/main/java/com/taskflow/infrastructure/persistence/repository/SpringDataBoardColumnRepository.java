package com.taskflow.infrastructure.persistence.repository;

import com.taskflow.infrastructure.persistence.entity.BoardColumnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataBoardColumnRepository extends JpaRepository<BoardColumnEntity, Long> {
    List<BoardColumnEntity> findByBoardIdOrderByPositionAsc(UUID boardId);
    void deleteByBoardId(UUID boardId);
}

package com.taskflow.infrastructure.persistence.repository;

import com.taskflow.infrastructure.persistence.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataBoardRepository extends JpaRepository<BoardEntity, UUID> {
}

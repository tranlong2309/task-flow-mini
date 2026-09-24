package com.taskflow.infrastructure.persistence.repository;

import com.taskflow.infrastructure.persistence.entity.BoardColumnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataBoardColumnRepository extends JpaRepository<BoardColumnEntity, Long> {
}

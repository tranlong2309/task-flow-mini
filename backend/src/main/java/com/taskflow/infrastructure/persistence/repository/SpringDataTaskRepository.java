package com.taskflow.infrastructure.persistence.repository;

import com.taskflow.infrastructure.persistence.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.time.Instant;

@Repository
public interface SpringDataTaskRepository extends JpaRepository<TaskEntity, UUID>, JpaSpecificationExecutor<TaskEntity> {
    List<TaskEntity> findByStatusColumnIdOrderByPositionAsc(Long statusColumnId);

    long countByBoardIdAndDeletedAtIsNull(UUID boardId);
    long countByBoardIdAndCompletedAtIsNotNullAndDeletedAtIsNull(UUID boardId);
    long countByBoardIdAndStatusColumnIdAndDeletedAtIsNull(UUID boardId, Long statusColumnId);
    long countByBoardIdAndIsBlockedTrueAndDeletedAtIsNull(UUID boardId);
    long countByBoardIdAndDueDateBeforeAndCompletedAtIsNullAndDeletedAtIsNull(UUID boardId, Instant date);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM TaskEntity t WHERE t.deletedAt IS NULL AND t.completedAt IS NULL AND (t.isBlocked IS NULL OR t.isBlocked = false) AND t.dueDate IS NOT NULL AND t.dueDate <= :upTo")
    java.util.List<TaskEntity> findTasksForReminders(@org.springframework.data.repository.query.Param("upTo") java.time.Instant upTo);

}

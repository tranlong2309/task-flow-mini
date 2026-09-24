package com.taskflow.infrastructure.persistence.repository;

import com.taskflow.infrastructure.persistence.entity.TaskHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataTaskHistoryRepository extends JpaRepository<TaskHistoryEntity, Long> {
}

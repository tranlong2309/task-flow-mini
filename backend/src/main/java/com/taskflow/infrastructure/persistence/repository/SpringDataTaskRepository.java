package com.taskflow.infrastructure.persistence.repository;

import com.taskflow.infrastructure.persistence.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataTaskRepository extends JpaRepository<TaskEntity, UUID>, JpaSpecificationExecutor<TaskEntity> {
}

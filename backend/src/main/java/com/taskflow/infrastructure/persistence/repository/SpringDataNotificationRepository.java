package com.taskflow.infrastructure.persistence.repository;

import com.taskflow.infrastructure.persistence.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface SpringDataNotificationRepository extends JpaRepository<NotificationEntity, Long> {
    boolean existsByTypeAndRelatedTaskIdAndCreatedAtAfter(String type, UUID relatedTaskId, Instant since);
}

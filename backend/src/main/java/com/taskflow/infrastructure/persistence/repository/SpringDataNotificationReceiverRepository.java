package com.taskflow.infrastructure.persistence.repository;

import com.taskflow.infrastructure.persistence.entity.NotificationReceiverEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataNotificationReceiverRepository extends JpaRepository<NotificationReceiverEntity, Long> {
    List<NotificationReceiverEntity> findByUserIdOrderByNotificationIdDesc(Long userId);
}

package com.taskflow.domain.repository;

import com.taskflow.domain.model.Notification;
import com.taskflow.domain.model.NotificationReceiver;
import com.taskflow.domain.model.UserNotification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepositoryPort {
    Notification saveNotification(Notification notification);
    void saveReceivers(List<NotificationReceiver> receivers);
    Optional<NotificationReceiver> findReceiverById(Long id);
    NotificationReceiver saveReceiver(NotificationReceiver receiver);
    boolean hasRecentNotification(String type, UUID relatedTaskId, Instant since);
    List<UserNotification> getUserNotifications(Long userId);
}

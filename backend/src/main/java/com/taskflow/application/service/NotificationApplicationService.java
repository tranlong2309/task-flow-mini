package com.taskflow.application.service;

import com.taskflow.application.port.in.GetNotificationsUseCase;
import com.taskflow.application.port.in.MarkNotificationReadUseCase;
import com.taskflow.domain.model.NotificationReceiver;
import com.taskflow.domain.model.UserNotification;
import com.taskflow.domain.repository.NotificationRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationApplicationService implements GetNotificationsUseCase, MarkNotificationReadUseCase {

    private final NotificationRepositoryPort notificationRepositoryPort;

    public NotificationApplicationService(NotificationRepositoryPort notificationRepositoryPort) {
        this.notificationRepositoryPort = notificationRepositoryPort;
    }

    @Override
    public List<UserNotification> getNotifications(Long userId) {
        return notificationRepositoryPort.getUserNotifications(userId);
    }

    @Override
    public NotificationReceiver markAsRead(Long receiverId, Long userId) {
        NotificationReceiver receiver = notificationRepositoryPort.findReceiverById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        
        if (!receiver.getUserId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Cannot mark other user's notification as read");
        }

        if (Boolean.TRUE.equals(receiver.getIsRead())) {
            return receiver;
        }

        receiver.setIsRead(true);
        receiver.setReadAt(Instant.now());
        
        return notificationRepositoryPort.saveReceiver(receiver);
    }
}

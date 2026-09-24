package com.taskflow.application.port.in;

import com.taskflow.domain.model.NotificationReceiver;

public interface MarkNotificationReadUseCase {
    NotificationReceiver markAsRead(Long receiverId, Long userId);
}

package com.taskflow.application.port.in;

import com.taskflow.domain.model.UserNotification;
import java.util.List;

public interface GetNotificationsUseCase {
    List<UserNotification> getNotifications(Long userId);
}

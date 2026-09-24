package com.taskflow.application.port.in;

import com.taskflow.domain.model.Task;

public interface SendNotificationUseCase {
    void sendTaskAssignedNotification(Task task);
    void sendTaskOverdueNotification(Task task);
    void sendTaskDeadlineSoonNotification(Task task, int daysLeft);
}

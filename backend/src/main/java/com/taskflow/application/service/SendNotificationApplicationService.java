package com.taskflow.application.service;

import com.taskflow.application.port.in.SendNotificationUseCase;
import com.taskflow.domain.model.Notification;
import com.taskflow.domain.model.NotificationReceiver;
import com.taskflow.domain.model.Task;
import com.taskflow.domain.repository.BoardMemberRepositoryPort;
import com.taskflow.domain.repository.NotificationRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SendNotificationApplicationService implements SendNotificationUseCase {

    private final NotificationRepositoryPort notificationRepositoryPort;
    private final BoardMemberRepositoryPort boardMemberRepositoryPort;

    public SendNotificationApplicationService(NotificationRepositoryPort notificationRepositoryPort, BoardMemberRepositoryPort boardMemberRepositoryPort) {
        this.notificationRepositoryPort = notificationRepositoryPort;
        this.boardMemberRepositoryPort = boardMemberRepositoryPort;
    }

    @Override
    @Transactional
    public void sendTaskAssignedNotification(Task task) {
        String type = "TASK_ASSIGNED";
        if (notificationRepositoryPort.hasRecentNotification(type, task.getId(), Instant.now().minus(24, ChronoUnit.HOURS))) {
            return;
        }

        Notification notification = new Notification(null, type, "Task Assigned", "You have been assigned to task: " + task.getTitle(), task.getId(), Instant.now(), null);
        notification = notificationRepositoryPort.saveNotification(notification);

        Set<Long> receivers = new HashSet<>(boardMemberRepositoryPort.findManagers(task.getBoardId()));
        if (task.getAssigneeId() != null) {
            receivers.add(task.getAssigneeId());
        }

        saveReceivers(notification.getId(), receivers);
    }

    @Override
    @Transactional
    public void sendTaskOverdueNotification(Task task) {
        String type = "TASK_OVERDUE";
        if (notificationRepositoryPort.hasRecentNotification(type, task.getId(), Instant.now().minus(24, ChronoUnit.HOURS))) {
            return;
        }

        Notification notification = new Notification(null, type, "Task Overdue", "Task is overdue: " + task.getTitle(), task.getId(), Instant.now(), null);
        notification = notificationRepositoryPort.saveNotification(notification);

        Set<Long> receivers = new HashSet<>(boardMemberRepositoryPort.findManagers(task.getBoardId()));
        if (task.getAssigneeId() != null) {
            receivers.add(task.getAssigneeId());
        }

        saveReceivers(notification.getId(), receivers);
    }

    @Override
    @Transactional
    public void sendTaskDeadlineSoonNotification(Task task, int daysLeft) {
        String type = "DEADLINE_SOON";
        if (notificationRepositoryPort.hasRecentNotification(type, task.getId(), Instant.now().minus(24, ChronoUnit.HOURS))) {
            return;
        }

        Notification notification = new Notification(null, type, "Task Deadline Soon", "Task '" + task.getTitle() + "' is due in " + daysLeft + " day(s).", task.getId(), Instant.now(), null);
        notification = notificationRepositoryPort.saveNotification(notification);

        Set<Long> receivers = new HashSet<>(boardMemberRepositoryPort.findManagers(task.getBoardId()));
        if (task.getAssigneeId() != null) {
            receivers.add(task.getAssigneeId());
        }

        saveReceivers(notification.getId(), receivers);
    }

    private void saveReceivers(Long notificationId, Set<Long> userIds) {
        List<NotificationReceiver> receivers = new ArrayList<>();
        for (Long userId : userIds) {
            receivers.add(new NotificationReceiver(null, notificationId, userId, false, null));
        }
        notificationRepositoryPort.saveReceivers(receivers);
    }
}

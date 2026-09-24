package com.taskflow.application.service;

import com.taskflow.domain.model.Notification;
import com.taskflow.domain.model.NotificationReceiver;
import com.taskflow.domain.model.Task;
import com.taskflow.domain.repository.BoardMemberRepositoryPort;
import com.taskflow.domain.repository.NotificationRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SendNotificationApplicationServiceTest {

    @Mock
    private NotificationRepositoryPort notificationRepositoryPort;

    @Mock
    private BoardMemberRepositoryPort boardMemberRepositoryPort;

    @InjectMocks
    private SendNotificationApplicationService sendNotificationApplicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendTaskAssignedNotification_shouldSend_whenNoRecentNotification() {
        UUID boardId = UUID.randomUUID();
        Task task = new Task(UUID.randomUUID(), boardId, "T1", "Desc", 1L, 10L, null, null, null, null, null, null, 0, null, false, null, null);

        when(notificationRepositoryPort.hasRecentNotification(anyString(), any(UUID.class), any(Instant.class))).thenReturn(false);
        when(boardMemberRepositoryPort.findManagers(boardId)).thenReturn(List.of(20L));
        when(notificationRepositoryPort.saveNotification(any())).thenAnswer(i -> {
            Notification n = i.getArgument(0);
            n.setId(100L);
            return n;
        });

        sendNotificationApplicationService.sendTaskAssignedNotification(task);

        verify(notificationRepositoryPort, times(1)).saveNotification(any());
        verify(notificationRepositoryPort, times(1)).saveReceivers(argThat(receivers -> receivers.size() == 2)); // assignee 10L and manager 20L
    }

    @Test
    void sendTaskAssignedNotification_shouldNotSend_whenRecentNotificationExists() {
        UUID boardId = UUID.randomUUID();
        Task task = new Task(UUID.randomUUID(), boardId, "T1", "Desc", 1L, 10L, null, null, null, null, null, null, 0, null, false, null, null);

        when(notificationRepositoryPort.hasRecentNotification(eq("TASK_ASSIGNED"), eq(task.getId()), any(Instant.class))).thenReturn(true);

        sendNotificationApplicationService.sendTaskAssignedNotification(task);

        verify(notificationRepositoryPort, never()).saveNotification(any());
        verify(notificationRepositoryPort, never()).saveReceivers(any());
    }
}

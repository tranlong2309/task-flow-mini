package com.taskflow.application.service;

import com.taskflow.application.port.in.GetBoardPermissionUseCase;
import com.taskflow.domain.model.BoardPermission;
import com.taskflow.domain.model.Priority;
import com.taskflow.domain.model.Task;
import com.taskflow.domain.repository.BoardMemberRepositoryPort;
import com.taskflow.domain.repository.TaskHistoryRepositoryPort;
import com.taskflow.domain.repository.TaskRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskApplicationServiceTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;
    @Mock
    private TaskHistoryRepositoryPort taskHistoryRepositoryPort;
    @Mock
    private BoardMemberRepositoryPort boardMemberRepositoryPort;
    @Mock
    private GetBoardPermissionUseCase getBoardPermissionUseCase;

    @InjectMocks
    private TaskApplicationService taskApplicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTask_shouldThrowException_whenTitleIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> {
            taskApplicationService.createTask(UUID.randomUUID(), "", "Desc", null, Priority.HIGH, Instant.now(), 1L, 1L);
        });
    }

    @Test
    void createTask_shouldThrowException_whenDueDateIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            taskApplicationService.createTask(UUID.randomUUID(), "Title", "Desc", null, Priority.HIGH, null, 1L, 1L);
        });
    }

    @Test
    void updateTask_shouldThrowException_whenUserDoesNotHavePermission() {
        UUID taskId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();
        Task existingTask = new Task(taskId, boardId, "Title", "Desc", 1L, 2L, Priority.HIGH, Instant.now(), 1L, Instant.now(), Instant.now(), null, 0, null, false, null, null);
        
        when(taskRepositoryPort.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(getBoardPermissionUseCase.getPermissions(boardId, 3L)).thenReturn(new BoardPermission(true, false, false, false)); // No edit permission

        assertThrows(AccessDeniedException.class, () -> {
            taskApplicationService.updateTask(taskId, "New Title", null, null, null, null, null, 3L);
        });
    }

    @Test
    void updateTask_shouldSucceed_whenUserIsAssignee() {
        UUID taskId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();
        Task existingTask = new Task(taskId, boardId, "Title", "Desc", 1L, 2L, Priority.HIGH, Instant.now(), 1L, Instant.now(), Instant.now(), null, 0, null, false, null, null);
        
        when(taskRepositoryPort.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task updatedTask = taskApplicationService.updateTask(taskId, "New Title", null, null, null, null, null, 2L);
        
        assertEquals("New Title", updatedTask.getTitle());
        verify(getBoardPermissionUseCase, never()).getPermissions(any(), any()); // Assignee bypasses board permission check
    }

    @Test
    void updateTask_shouldRecordHistory_whenAssigneeIsChanged() {
        UUID taskId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();
        Task existingTask = new Task(taskId, boardId, "Title", "Desc", 1L, 2L, Priority.HIGH, Instant.now(), 1L, Instant.now(), Instant.now(), null, 0, null, false, null, null);
        
        when(taskRepositoryPort.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(getBoardPermissionUseCase.getPermissions(boardId, 3L)).thenReturn(new BoardPermission(true, true, false, false)); // Has edit permission
        when(boardMemberRepositoryPort.getRoleInBoard(boardId, 4L)).thenReturn(Optional.of("MEMBER"));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        taskApplicationService.updateTask(taskId, null, null, 4L, null, null, null, 3L);
        
        verify(taskHistoryRepositoryPort).save(argThat(history -> 
            history.getTaskId().equals(taskId) && 
            "assignee_id".equals(history.getFieldName()) && 
            "2".equals(history.getOldValue()) && 
            "4".equals(history.getNewValue()) && 
            history.getChangedBy().equals(3L)
        ));
    }
}

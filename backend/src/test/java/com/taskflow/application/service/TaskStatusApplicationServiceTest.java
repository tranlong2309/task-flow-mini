package com.taskflow.application.service;

import com.taskflow.application.port.in.GetBoardPermissionUseCase;
import com.taskflow.domain.model.BoardColumn;
import com.taskflow.domain.model.BoardPermission;
import com.taskflow.domain.model.Priority;
import com.taskflow.domain.model.Task;
import com.taskflow.domain.repository.BoardColumnRepositoryPort;
import com.taskflow.domain.repository.TaskHistoryRepositoryPort;
import com.taskflow.domain.repository.TaskRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskStatusApplicationServiceTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;
    @Mock
    private TaskHistoryRepositoryPort taskHistoryRepositoryPort;
    @Mock
    private BoardColumnRepositoryPort boardColumnRepositoryPort;
    @Mock
    private GetBoardPermissionUseCase getBoardPermissionUseCase;

    @InjectMocks
    private TaskStatusApplicationService taskStatusApplicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void moveTask_shouldReorderWithinSameColumn() {
        UUID taskId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();
        Task existingTask = new Task(taskId, boardId, "Title", "Desc", 1L, 2L, Priority.HIGH, Instant.now(), 1L, Instant.now(), Instant.now(), null, 0, null, false, null, null);
        Task otherTask = new Task(UUID.randomUUID(), boardId, "Title2", "Desc", 1L, 2L, Priority.HIGH, Instant.now(), 1L, Instant.now(), Instant.now(), null, 1, null, false, null, null);

        BoardColumn col = new BoardColumn(1L, boardId, "Todo", 0, "gray");

        List<Task> tasks = new ArrayList<>();
        tasks.add(existingTask);
        tasks.add(otherTask);

        when(taskRepositoryPort.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(boardColumnRepositoryPort.findById(1L)).thenReturn(Optional.of(col));
        when(taskRepositoryPort.findByStatusColumnIdOrderByPositionAsc(1L)).thenReturn(tasks);
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task movedTask = taskStatusApplicationService.moveTask(taskId, 1L, 1L, 0, 1, 2L);

        assertEquals(1, movedTask.getPosition()); // It should move to index 1
        verify(taskRepositoryPort).saveAll(argThat(list -> 
            list.size() == 2 && list.get(1).getId().equals(taskId) && list.get(1).getPosition() == 1
        ));
    }

    @Test
    void moveTask_shouldMoveToDifferentColumnAndSetCompletedAt() {
        UUID taskId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();
        Task existingTask = new Task(taskId, boardId, "Title", "Desc", 1L, 2L, Priority.HIGH, Instant.now(), 1L, Instant.now(), Instant.now(), null, 0, null, false, null, null);

        BoardColumn srcCol = new BoardColumn(1L, boardId, "Todo", 0, "gray");
        BoardColumn tgtCol = new BoardColumn(2L, boardId, "Done", 1, "gray");

        List<Task> srcTasks = new ArrayList<>();
        srcTasks.add(existingTask);

        List<Task> tgtTasks = new ArrayList<>();

        when(taskRepositoryPort.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(boardColumnRepositoryPort.findById(1L)).thenReturn(Optional.of(srcCol));
        when(boardColumnRepositoryPort.findById(2L)).thenReturn(Optional.of(tgtCol));
        when(taskRepositoryPort.findByStatusColumnIdOrderByPositionAsc(1L)).thenReturn(srcTasks);
        when(taskRepositoryPort.findByStatusColumnIdOrderByPositionAsc(2L)).thenReturn(tgtTasks);
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task movedTask = taskStatusApplicationService.moveTask(taskId, 1L, 2L, 0, 0, 2L);

        assertEquals(2L, movedTask.getStatusColumnId());
        assertNotNull(movedTask.getCompletedAt()); // Done column should set completedAt
    }

    @Test
    void updateTaskStatus_shouldThrowException_whenTargetColumnNotBelongToBoard() {
        UUID taskId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();
        Task existingTask = new Task(taskId, boardId, "Title", "Desc", 1L, 2L, Priority.HIGH, Instant.now(), 1L, Instant.now(), Instant.now(), null, 0, null, false, null, null);

        BoardColumn tgtCol = new BoardColumn(2L, UUID.randomUUID(), "Done", 1, "gray"); // Different boardId

        when(taskRepositoryPort.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(boardColumnRepositoryPort.findById(2L)).thenReturn(Optional.of(tgtCol));

        assertThrows(IllegalArgumentException.class, () -> {
            taskStatusApplicationService.updateTaskStatus(taskId, 2L, "note", 2L);
        });
    }

    @Test
    void blockTask_shouldThrowException_whenReasonIsBlank() {
        UUID taskId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> {
            taskStatusApplicationService.blockTask(taskId, " ", 2L);
        });
    }

    @Test
    void blockTask_shouldSetIsBlockedAndReason() {
        UUID taskId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();
        Task existingTask = new Task(taskId, boardId, "Title", "Desc", 1L, 2L, Priority.HIGH, Instant.now(), 1L, Instant.now(), Instant.now(), null, 0, null, false, null, null);

        BoardColumn srcCol = new BoardColumn(1L, boardId, "Todo", 0, "gray");

        when(taskRepositoryPort.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(boardColumnRepositoryPort.findById(1L)).thenReturn(Optional.of(srcCol));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task blockedTask = taskStatusApplicationService.blockTask(taskId, "Waiting for client", 2L);

        assertTrue(blockedTask.getIsBlocked());
        assertEquals("Waiting for client", blockedTask.getBlockedReason());
        assertNotNull(blockedTask.getBlockedAt());
    }

    @Test
    void unblockTask_shouldClearIsBlockedAndReason() {
        UUID taskId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();
        Task blockedTask = new Task(taskId, boardId, "Title", "Desc", 1L, 2L, Priority.HIGH, Instant.now(), 1L, Instant.now(), Instant.now(), null, 0, null, true, "Reason", Instant.now());

        BoardColumn srcCol = new BoardColumn(1L, boardId, "Todo", 0, "gray");

        when(taskRepositoryPort.findById(taskId)).thenReturn(Optional.of(blockedTask));
        when(boardColumnRepositoryPort.findById(1L)).thenReturn(Optional.of(srcCol));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task unblockedTask = taskStatusApplicationService.unblockTask(taskId, 2L);

        assertFalse(unblockedTask.getIsBlocked());
        assertNull(unblockedTask.getBlockedReason());
        assertNull(unblockedTask.getBlockedAt());
    }
}

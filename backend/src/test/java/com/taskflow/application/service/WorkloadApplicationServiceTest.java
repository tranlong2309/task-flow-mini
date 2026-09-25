package com.taskflow.application.service;

import com.taskflow.application.port.in.GetBoardPermissionUseCase;
import com.taskflow.domain.model.BoardColumn;
import com.taskflow.domain.model.BoardPermission;
import com.taskflow.domain.model.BoardWorkloadSummary;
import com.taskflow.domain.model.Priority;
import com.taskflow.domain.model.Task;
import com.taskflow.domain.model.User;
import com.taskflow.domain.repository.BoardColumnRepositoryPort;
import com.taskflow.domain.repository.TaskRepositoryPort;
import com.taskflow.domain.repository.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import com.taskflow.infrastructure.web.dto.PagedResponse;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class WorkloadApplicationServiceTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;
    @Mock
    private BoardColumnRepositoryPort boardColumnRepositoryPort;
    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private GetBoardPermissionUseCase getBoardPermissionUseCase;

    @InjectMocks
    private WorkloadApplicationService workloadApplicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getWorkload_shouldReturnCorrectSummary() {
        UUID boardId = UUID.randomUUID();
        Long userId = 1L;

        when(getBoardPermissionUseCase.getPermissions(boardId, userId))
                .thenReturn(new BoardPermission(true, true, true, true)); // Admin

        BoardColumn col1 = new BoardColumn(1L, boardId, "Todo", 0, "gray");
        BoardColumn col2 = new BoardColumn(2L, boardId, "Done", 1, "gray");
        when(boardColumnRepositoryPort.findByBoardId(boardId)).thenReturn(List.of(col1, col2));

        Instant past = Instant.now().minus(1, ChronoUnit.DAYS);
        
        Task task1 = new Task(UUID.randomUUID(), boardId, "T1", "Desc", 1L, 2L, Priority.HIGH, past, 1L, Instant.now(), Instant.now(), null, 0, null, false, null, null); // Overdue
        Task task2 = new Task(UUID.randomUUID(), boardId, "T2", "Desc", 1L, 3L, Priority.HIGH, Instant.now().plus(1, ChronoUnit.DAYS), 1L, Instant.now(), Instant.now(), null, 1, null, true, "blocked", Instant.now()); // Blocked
        Task task3 = new Task(UUID.randomUUID(), boardId, "T3", "Desc", 2L, 2L, Priority.HIGH, past, 1L, Instant.now(), Instant.now(), null, 0, Instant.now(), false, null, null); // Done

        when(taskRepositoryPort.searchTasks(eq(boardId), isNull(), isNull(), isNull(), isNull(), isNull(), 
                                            isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), 
                                            anyInt(), anyInt()))
                .thenReturn(new PagedResponse<>(List.of(task1, task2, task3), 3, 1, 0, 100000));

        User user2 = new User(2L, "User 2", "user2@test.com", "MEMBER", List.of());
        User user3 = new User(3L, "User 3", "user3@test.com", "MEMBER", List.of());
        when(userRepositoryPort.findByIds(any())).thenReturn(List.of(user2, user3));

        BoardWorkloadSummary summary = workloadApplicationService.getWorkload(boardId, userId);

        assertEquals(3, summary.getSummary().get("total"));
        assertEquals(2, summary.getSummary().get("Todo"));
        assertEquals(1, summary.getSummary().get("Done"));
        assertEquals(1, summary.getSummary().get("overdue"));
        assertEquals(1, summary.getSummary().get("blocked"));

        assertEquals(2, summary.getByAssignee().size());
        
        // Assert user 2 (has 1 active overdue task and 1 done task, total 2 tasks, 1 active)
        var assignee2 = summary.getByAssignee().stream().filter(a -> a.getAssigneeId().equals(2L)).findFirst().get();
        assertEquals(1, assignee2.getActiveTasks());
        assertEquals(1, assignee2.getOverdueTasks());
        assertEquals(0, assignee2.getBlockedTasks());
        
        // Assert user 3
        var assignee3 = summary.getByAssignee().stream().filter(a -> a.getAssigneeId().equals(3L)).findFirst().get();
        assertEquals(1, assignee3.getActiveTasks());
        assertEquals(0, assignee3.getOverdueTasks());
        assertEquals(1, assignee3.getBlockedTasks());
    }
}

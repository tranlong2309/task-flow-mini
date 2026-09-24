package com.taskflow.application.service;

import com.taskflow.application.port.in.GetBoardPermissionUseCase;
import com.taskflow.application.port.in.GetWorkloadUseCase;
import com.taskflow.domain.model.*;
import com.taskflow.domain.repository.BoardColumnRepositoryPort;
import com.taskflow.domain.repository.TaskRepositoryPort;
import com.taskflow.domain.repository.UserRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkloadApplicationService implements GetWorkloadUseCase {

    private final TaskRepositoryPort taskRepositoryPort;
    private final BoardColumnRepositoryPort boardColumnRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final GetBoardPermissionUseCase getBoardPermissionUseCase;

    public WorkloadApplicationService(TaskRepositoryPort taskRepositoryPort,
                                      BoardColumnRepositoryPort boardColumnRepositoryPort,
                                      UserRepositoryPort userRepositoryPort,
                                      GetBoardPermissionUseCase getBoardPermissionUseCase) {
        this.taskRepositoryPort = taskRepositoryPort;
        this.boardColumnRepositoryPort = boardColumnRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.getBoardPermissionUseCase = getBoardPermissionUseCase;
    }

    @Override
    public BoardWorkloadSummary getWorkload(UUID boardId, Long userId) {
        BoardPermission permission = getBoardPermissionUseCase.getPermissions(boardId, userId);
        boolean isManagerOrAdmin = permission.isCanDeleteBoard(); // simple check, normally manager has more permissions

        List<Task> tasks = taskRepositoryPort.searchTasks(boardId, null, null, null, null, null);
        
        if (!isManagerOrAdmin) {
            tasks = tasks.stream()
                    .filter(t -> Objects.equals(t.getAssigneeId(), userId))
                    .collect(Collectors.toList());
        }

        Map<Long, String> columnNames = boardColumnRepositoryPort.findByBoardId(boardId).stream()
                .collect(Collectors.toMap(BoardColumn::getId, BoardColumn::getName));

        Map<String, Long> summary = new HashMap<>();
        summary.put("total", (long) tasks.size());
        
        long overdueCount = 0;
        long blockedCount = 0;

        for (Task task : tasks) {
            if (Boolean.TRUE.equals(task.getIsBlocked())) {
                blockedCount++;
            }
            if (task.getCompletedAt() == null && task.getDueDate() != null && task.getDueDate().isBefore(Instant.now())) {
                overdueCount++;
            }
            
            if (task.getStatusColumnId() != null && columnNames.containsKey(task.getStatusColumnId())) {
                String colName = columnNames.get(task.getStatusColumnId());
                summary.put(colName, summary.getOrDefault(colName, 0L) + 1);
            }
        }
        
        summary.put("overdue", overdueCount);
        summary.put("blocked", blockedCount);

        // Group by assignee
        Map<Long, List<Task>> tasksByAssignee = tasks.stream()
                .filter(t -> t.getAssigneeId() != null)
                .collect(Collectors.groupingBy(Task::getAssigneeId));

        List<Long> assigneeIds = new ArrayList<>(tasksByAssignee.keySet());
        Map<Long, String> userNames = userRepositoryPort.findByIds(assigneeIds).stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        List<AssigneeWorkload> assigneeWorkloads = new ArrayList<>();
        for (Map.Entry<Long, List<Task>> entry : tasksByAssignee.entrySet()) {
            Long assigneeId = entry.getKey();
            List<Task> userTasks = entry.getValue();
            
            long activeTasks = userTasks.stream().filter(t -> t.getCompletedAt() == null).count();
            long userOverdue = userTasks.stream().filter(t -> t.getCompletedAt() == null && t.getDueDate() != null && t.getDueDate().isBefore(Instant.now())).count();
            long userBlocked = userTasks.stream().filter(t -> Boolean.TRUE.equals(t.getIsBlocked())).count();
            
            String fullName = userNames.getOrDefault(assigneeId, "Unknown");
            assigneeWorkloads.add(new AssigneeWorkload(assigneeId, fullName, activeTasks, userOverdue, userBlocked));
        }

        return new BoardWorkloadSummary(boardId, summary, assigneeWorkloads);
    }
}

package com.taskflow.application.service;

import com.taskflow.application.port.in.GetBoardPermissionUseCase;
import com.taskflow.application.port.in.MoveTaskUseCase;
import com.taskflow.application.port.in.UpdateTaskStatusUseCase;
import com.taskflow.domain.model.BoardColumn;
import com.taskflow.domain.model.BoardPermission;
import com.taskflow.domain.model.Task;
import com.taskflow.domain.model.TaskHistory;
import com.taskflow.domain.repository.BoardColumnRepositoryPort;
import com.taskflow.domain.repository.TaskHistoryRepositoryPort;
import com.taskflow.domain.repository.TaskRepositoryPort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class TaskStatusApplicationService implements UpdateTaskStatusUseCase, MoveTaskUseCase {

    private final TaskRepositoryPort taskRepositoryPort;
    private final TaskHistoryRepositoryPort taskHistoryRepositoryPort;
    private final BoardColumnRepositoryPort boardColumnRepositoryPort;
    private final GetBoardPermissionUseCase getBoardPermissionUseCase;

    public TaskStatusApplicationService(TaskRepositoryPort taskRepositoryPort,
                                        TaskHistoryRepositoryPort taskHistoryRepositoryPort,
                                        BoardColumnRepositoryPort boardColumnRepositoryPort,
                                        GetBoardPermissionUseCase getBoardPermissionUseCase) {
        this.taskRepositoryPort = taskRepositoryPort;
        this.taskHistoryRepositoryPort = taskHistoryRepositoryPort;
        this.boardColumnRepositoryPort = boardColumnRepositoryPort;
        this.getBoardPermissionUseCase = getBoardPermissionUseCase;
    }

    @Override
    @Transactional
    public Task updateTaskStatus(UUID taskId, Long statusColumnId, String note, Long updaterId) {
        Task task = getTaskAndVerifyPermission(taskId, updaterId);

        if (Objects.equals(task.getStatusColumnId(), statusColumnId)) {
            return task; // No change
        }

        BoardColumn targetColumn = getColumnAndVerifyBoard(statusColumnId, task.getBoardId());
        
        String oldColumnStr = String.valueOf(task.getStatusColumnId());
        task.setStatusColumnId(statusColumnId);
        
        handleDoneStatus(task, targetColumn);
        
        task.setUpdatedAt(Instant.now());
        task = taskRepositoryPort.save(task);

        recordHistory(taskId, "status_column_id", oldColumnStr, String.valueOf(statusColumnId), updaterId);
        
        if (note != null && !note.trim().isEmpty()) {
            recordHistory(taskId, "note", null, note, updaterId);
        }

        return task;
    }

    @Override
    @Transactional
    public Task moveTask(UUID taskId, Long sourceColumnId, Long targetColumnId, int sourceIndex, int targetIndex, Long updaterId) {
        Task task = getTaskAndVerifyPermission(taskId, updaterId);

        BoardColumn srcCol = getColumnAndVerifyBoard(sourceColumnId, task.getBoardId());
        BoardColumn tgtCol = getColumnAndVerifyBoard(targetColumnId, task.getBoardId());

        if (!Objects.equals(task.getStatusColumnId(), sourceColumnId)) {
            throw new IllegalArgumentException("Task is not in the source column");
        }

        boolean columnChanged = !sourceColumnId.equals(targetColumnId);
        String oldColumnStr = String.valueOf(task.getStatusColumnId());

        if (columnChanged) {
            // Remove from source
            List<Task> sourceTasks = taskRepositoryPort.findByStatusColumnIdOrderByPositionAsc(sourceColumnId);
            sourceTasks.removeIf(t -> t.getId().equals(taskId));
            for (int i = 0; i < sourceTasks.size(); i++) {
                sourceTasks.get(i).setPosition(i);
            }
            taskRepositoryPort.saveAll(sourceTasks);

            // Insert into target
            List<Task> targetTasks = taskRepositoryPort.findByStatusColumnIdOrderByPositionAsc(targetColumnId);
            if (targetIndex > targetTasks.size()) {
                targetIndex = targetTasks.size();
            }
            targetTasks.add(targetIndex, task);
            for (int i = 0; i < targetTasks.size(); i++) {
                targetTasks.get(i).setPosition(i);
            }
            task.setStatusColumnId(targetColumnId);
            handleDoneStatus(task, tgtCol);
            taskRepositoryPort.saveAll(targetTasks);
            
            recordHistory(taskId, "status_column_id", oldColumnStr, String.valueOf(targetColumnId), updaterId);
        } else {
            // Reorder within the same column
            List<Task> columnTasks = taskRepositoryPort.findByStatusColumnIdOrderByPositionAsc(sourceColumnId);
            columnTasks.removeIf(t -> t.getId().equals(taskId));
            
            if (targetIndex > columnTasks.size()) {
                targetIndex = columnTasks.size();
            }
            
            columnTasks.add(targetIndex, task);
            for (int i = 0; i < columnTasks.size(); i++) {
                columnTasks.get(i).setPosition(i);
            }
            taskRepositoryPort.saveAll(columnTasks);
        }

        task.setUpdatedAt(Instant.now());
        return taskRepositoryPort.save(task);
    }

    private Task getTaskAndVerifyPermission(UUID taskId, Long userId) {
        Task task = taskRepositoryPort.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        if (task.getDeletedAt() != null) {
            throw new IllegalArgumentException("Cannot modify a deleted task");
        }

        if (!Objects.equals(task.getAssigneeId(), userId)) {
            BoardPermission permission = getBoardPermissionUseCase.getPermissions(task.getBoardId(), userId);
            if (!permission.isCanEditTask()) {
                throw new AccessDeniedException("User does not have permission to edit task");
            }
        }
        return task;
    }

    private BoardColumn getColumnAndVerifyBoard(Long columnId, UUID boardId) {
        BoardColumn column = boardColumnRepositoryPort.findById(columnId)
                .orElseThrow(() -> new IllegalArgumentException("Column not found: " + columnId));
        if (!column.getBoardId().equals(boardId)) {
            throw new IllegalArgumentException("Column does not belong to the task's board");
        }
        return column;
    }

    private void handleDoneStatus(Task task, BoardColumn targetColumn) {
        if ("Done".equalsIgnoreCase(targetColumn.getName())) {
            if (task.getCompletedAt() == null) {
                task.setCompletedAt(Instant.now());
            }
        } else {
            task.setCompletedAt(null);
        }
    }

    private void recordHistory(UUID taskId, String fieldName, String oldValue, String newValue, Long updaterId) {
        TaskHistory history = new TaskHistory(null, taskId, fieldName, oldValue, newValue, updaterId, Instant.now());
        taskHistoryRepositoryPort.save(history);
    }
}

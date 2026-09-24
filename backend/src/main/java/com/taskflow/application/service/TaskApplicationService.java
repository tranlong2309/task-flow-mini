package com.taskflow.application.service;

import com.taskflow.infrastructure.web.dto.PagedResponse;

import com.taskflow.application.port.in.CreateTaskUseCase;
import com.taskflow.application.port.in.DeleteTaskUseCase;
import com.taskflow.application.port.in.GetBoardPermissionUseCase;
import com.taskflow.application.port.in.GetTaskUseCase;
import com.taskflow.application.port.in.SearchTasksUseCase;
import com.taskflow.application.port.in.UpdateTaskUseCase;
import com.taskflow.domain.model.BoardPermission;
import com.taskflow.domain.model.Priority;
import com.taskflow.domain.model.Task;
import com.taskflow.domain.model.TaskHistory;
import com.taskflow.domain.repository.BoardMemberRepositoryPort;
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
public class TaskApplicationService implements CreateTaskUseCase, UpdateTaskUseCase, GetTaskUseCase, SearchTasksUseCase, DeleteTaskUseCase {

    private final TaskRepositoryPort taskRepositoryPort;
    private final TaskHistoryRepositoryPort taskHistoryRepositoryPort;
    private final BoardMemberRepositoryPort boardMemberRepositoryPort;
    private final GetBoardPermissionUseCase getBoardPermissionUseCase;
    private final com.taskflow.application.port.in.SendNotificationUseCase sendNotificationUseCase;

    public TaskApplicationService(TaskRepositoryPort taskRepositoryPort,
                                  TaskHistoryRepositoryPort taskHistoryRepositoryPort,
                                  BoardMemberRepositoryPort boardMemberRepositoryPort,
                                  GetBoardPermissionUseCase getBoardPermissionUseCase,
                                  com.taskflow.application.port.in.SendNotificationUseCase sendNotificationUseCase) {
        this.taskRepositoryPort = taskRepositoryPort;
        this.taskHistoryRepositoryPort = taskHistoryRepositoryPort;
        this.boardMemberRepositoryPort = boardMemberRepositoryPort;
        this.getBoardPermissionUseCase = getBoardPermissionUseCase;
        this.sendNotificationUseCase = sendNotificationUseCase;
    }

    @Override
    @Transactional
    public Task createTask(UUID boardId, String title, String description, Long assigneeId, Priority priority, Instant dueDate, Long statusColumnId, Long createdBy) {
        validateTitle(title);
        
        if (dueDate == null) {
            throw new IllegalArgumentException("dueDate must not be null");
        }

        if (assigneeId != null) {
            verifyUserInBoard(boardId, assigneeId);
        }

        BoardPermission permission = getBoardPermissionUseCase.getPermissions(boardId, createdBy);
        if (!permission.isCanCreateTask()) {
            throw new AccessDeniedException("User does not have permission to create task");
        }

        Task task = new Task(UUID.randomUUID(), boardId, title, description, statusColumnId, assigneeId, priority, dueDate, createdBy, Instant.now(), Instant.now(), null, 0, null, false, null, null);
        task = taskRepositoryPort.save(task);
        if (task.getAssigneeId() != null) {
            sendNotificationUseCase.sendTaskAssignedNotification(task);
        }
        return task;
    }

    @Override
    @Transactional
    public Task updateTask(UUID taskId, String title, String description, Long assigneeId, Priority priority, Instant dueDate, Long statusColumnId, Long updaterId) {
        Task task = taskRepositoryPort.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
        
        if (task.getDeletedAt() != null) {
            throw new IllegalArgumentException("Cannot update a deleted task");
        }

        verifyCanUpdateTask(task, updaterId);

        if (title != null) {
            validateTitle(title);
            task.setTitle(title);
        }

        if (description != null) {
            task.setDescription(description);
        }

        boolean assigneeChanged = false;

        if (assigneeId != null && !Objects.equals(task.getAssigneeId(), assigneeId)) {
            verifyUserInBoard(task.getBoardId(), assigneeId);
            recordHistory(taskId, "assignee_id", String.valueOf(task.getAssigneeId()), String.valueOf(assigneeId), updaterId);
            task.setAssigneeId(assigneeId);
            assigneeChanged = true;
        }

        if (statusColumnId != null && !Objects.equals(task.getStatusColumnId(), statusColumnId)) {
            recordHistory(taskId, "status_column_id", String.valueOf(task.getStatusColumnId()), String.valueOf(statusColumnId), updaterId);
            task.setStatusColumnId(statusColumnId);
        }

        if (priority != null) {
            task.setPriority(priority);
        }

        if (dueDate != null) {
            task.setDueDate(dueDate);
        }
        
        task.setUpdatedAt(Instant.now());
        task = taskRepositoryPort.save(task);

        if (assigneeChanged) {
            sendNotificationUseCase.sendTaskAssignedNotification(task);
        }

        return task;
    }

    @Override
    public Task getTask(UUID taskId, Long requesterId) {
        Task task = taskRepositoryPort.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
        
        if (task.getDeletedAt() != null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }

        getBoardPermissionUseCase.getPermissions(task.getBoardId(), requesterId);
        return task;
    }

    @Override
    public PagedResponse<Task> searchTasks(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly, Long requesterId, int page, int size) {
        getBoardPermissionUseCase.getPermissions(boardId, requesterId);
        return taskRepositoryPort.searchTasks(boardId, statusColumnId, assigneeId, priority, search, overdueOnly, page, size);
    }

    @Override
    @Transactional
    public void deleteTask(UUID taskId, Long requesterId) {
        Task task = taskRepositoryPort.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
        
        verifyCanUpdateTask(task, requesterId);
        
        task.setDeletedAt(Instant.now());
        task.setUpdatedAt(Instant.now());
        taskRepositoryPort.save(task);
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (title.length() > 255) {
            throw new IllegalArgumentException("title must not exceed 255 characters");
        }
    }

    private void verifyUserInBoard(UUID boardId, Long userId) {
        boardMemberRepositoryPort.getRoleInBoard(boardId, userId)
                .orElseThrow(() -> new IllegalArgumentException("assigneeId must belong to the board"));
    }

    private void verifyCanUpdateTask(Task task, Long userId) {
        if (Objects.equals(task.getAssigneeId(), userId)) {
            return;
        }

        BoardPermission permission = getBoardPermissionUseCase.getPermissions(task.getBoardId(), userId);
        if (!permission.isCanEditTask()) {
            throw new AccessDeniedException("User does not have permission to edit task");
        }
    }

    private void recordHistory(UUID taskId, String fieldName, String oldValue, String newValue, Long updaterId) {
        TaskHistory history = new TaskHistory(null, taskId, fieldName, oldValue, newValue, updaterId, Instant.now());
        taskHistoryRepositoryPort.save(history);
    }
}

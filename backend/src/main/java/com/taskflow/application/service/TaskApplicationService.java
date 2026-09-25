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
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

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
    public Task createTask(UUID boardId, String title, String description, Set<Long> assigneeIds, Priority priority, Instant dueDate, Long statusColumnId, Long createdBy) {
        validateTitle(title);
        
        if (dueDate == null) {
            throw new IllegalArgumentException("dueDate must not be null");
        }

        if (assigneeIds != null) {
            for (Long assigneeId : assigneeIds) {
                verifyUserInBoard(boardId, assigneeId);
            }
        } else {
            assigneeIds = new HashSet<>();
        }

        BoardPermission permission = getBoardPermissionUseCase.getPermissions(boardId, createdBy);
        if (!permission.isCanCreateTask()) {
            throw new AccessDeniedException("User does not have permission to create task");
        }

        Task task = new Task(UUID.randomUUID(), boardId, title, description, statusColumnId, assigneeIds, null, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), priority, dueDate, createdBy, Instant.now(), Instant.now(), null, 0, null, false, null, null);
        task = taskRepositoryPort.save(task);
        if (!task.getAssigneeIds().isEmpty()) {
            sendNotificationUseCase.sendTaskAssignedNotification(task);
        }
        return task;
    }

    @Override
    @Transactional
    public Task updateTask(UUID taskId, String title, String description, Set<Long> assigneeIds, 
                           Priority priority, Instant dueDate, Long statusColumnId, 
                           Instant assignedDate, Instant startDate, 
                           List<com.taskflow.domain.model.Subtask> subtasks, 
                           List<com.taskflow.domain.model.Comment> comments, 
                           List<com.taskflow.domain.model.Attachment> attachments,
                           Long updaterId) {
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

        if (assigneeIds != null && !Objects.equals(task.getAssigneeIds(), assigneeIds)) {
            for (Long assigneeId : assigneeIds) {
                verifyUserInBoard(task.getBoardId(), assigneeId);
            }
            recordHistory(taskId, "assignee_ids", String.valueOf(task.getAssigneeIds()), String.valueOf(assigneeIds), updaterId);
            task.setAssigneeIds(assigneeIds);
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
        
        if (assignedDate != null) {
            task.setAssignedDate(assignedDate);
        }
        
        if (startDate != null) {
            task.setStartDate(startDate);
        }

        if (subtasks != null) {
            task.setSubtasks(subtasks);
        }

        if (comments != null) {
            task.setComments(comments);
        }

        if (attachments != null) {
            task.setAttachments(attachments);
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
    public PagedResponse<Task> searchTasks(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly, 
                                           Instant assignedDateFrom, Instant assignedDateTo, 
                                           Instant startDateFrom, Instant startDateTo, 
                                           Instant endDateFrom, Instant endDateTo, 
                                           Long requesterId, int page, int size) {
        getBoardPermissionUseCase.getPermissions(boardId, requesterId);
        return taskRepositoryPort.searchTasks(boardId, statusColumnId, assigneeId, priority, search, overdueOnly, 
                                              assignedDateFrom, assignedDateTo, startDateFrom, startDateTo, endDateFrom, endDateTo, 
                                              page, size);
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
        if (task.getAssigneeIds() != null && task.getAssigneeIds().contains(userId)) {
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

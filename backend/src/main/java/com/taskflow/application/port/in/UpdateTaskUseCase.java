package com.taskflow.application.port.in;

import com.taskflow.domain.model.Priority;
import com.taskflow.domain.model.Task;

import java.time.Instant;
import java.util.UUID;

import java.util.Set;

import java.util.List;
import com.taskflow.domain.model.Subtask;
import com.taskflow.domain.model.Comment;
import com.taskflow.domain.model.Attachment;

public interface UpdateTaskUseCase {
    Task updateTask(UUID taskId, String title, String description, Set<Long> assigneeIds, 
                    Priority priority, Instant dueDate, Long statusColumnId, 
                    Instant assignedDate, Instant startDate, 
                    List<Subtask> subtasks, List<Comment> comments, List<Attachment> attachments,
                    Long updaterId);
}

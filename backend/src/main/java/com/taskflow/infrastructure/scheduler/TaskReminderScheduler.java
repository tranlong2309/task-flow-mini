package com.taskflow.infrastructure.scheduler;

import com.taskflow.application.port.in.SendNotificationUseCase;
import com.taskflow.domain.model.Task;
import com.taskflow.infrastructure.persistence.repository.SpringDataTaskRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
@EnableScheduling
public class TaskReminderScheduler {

    private final SpringDataTaskRepository taskRepository;
    private final SendNotificationUseCase sendNotificationUseCase;

    public TaskReminderScheduler(SpringDataTaskRepository taskRepository, SendNotificationUseCase sendNotificationUseCase) {
        this.taskRepository = taskRepository;
        this.sendNotificationUseCase = sendNotificationUseCase;
    }

    // Run every hour
    @Scheduled(cron = "0 0 * * * *")
    public void runReminders() {
        // Find tasks that are not deleted, not completed, not blocked, and have a due date
        List<com.taskflow.infrastructure.persistence.entity.TaskEntity> entities = taskRepository.findAll().stream()
                .filter(t -> t.getDeletedAt() == null && t.getCompletedAt() == null && !Boolean.TRUE.equals(t.getIsBlocked()) && t.getDueDate() != null)
                .collect(Collectors.toList());

        Instant now = Instant.now();

        for (com.taskflow.infrastructure.persistence.entity.TaskEntity entity : entities) {
            Task task = toDomain(entity);
            Instant dueDate = task.getDueDate();
            
            if (dueDate.isBefore(now)) {
                sendNotificationUseCase.sendTaskOverdueNotification(task);
            } else {
                long daysLeft = ChronoUnit.DAYS.between(now, dueDate);
                if (daysLeft == 1 || daysLeft == 3 || daysLeft == 7) {
                    sendNotificationUseCase.sendTaskDeadlineSoonNotification(task, (int) daysLeft);
                }
            }
        }
    }
    
    // Test endpoint trigger
    public void triggerManually() {
        runReminders();
    }

    private Task toDomain(com.taskflow.infrastructure.persistence.entity.TaskEntity entity) {
        return new Task(
                entity.getId(),
                entity.getBoardId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatusColumnId(),
                entity.getAssigneeId(),
                entity.getPriority(),
                entity.getDueDate(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getPosition(),
                entity.getCompletedAt(),
                entity.getIsBlocked(),
                entity.getBlockedReason(),
                entity.getBlockedAt()
        );
    }
}

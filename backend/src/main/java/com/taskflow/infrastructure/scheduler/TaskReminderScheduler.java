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

    private final com.taskflow.domain.repository.TaskRepositoryPort taskRepositoryPort;
    private final SendNotificationUseCase sendNotificationUseCase;

    public TaskReminderScheduler(com.taskflow.domain.repository.TaskRepositoryPort taskRepositoryPort, SendNotificationUseCase sendNotificationUseCase) {
        this.taskRepositoryPort = taskRepositoryPort;
        this.sendNotificationUseCase = sendNotificationUseCase;
    }

    // Run every hour
    @Scheduled(cron = "0 0 * * * *")
    public void runReminders() {
        // Find tasks that are not deleted, not completed, not blocked, and have a due date
        Instant now = Instant.now();
        java.time.Instant upTo = now.plus(8, ChronoUnit.DAYS); // Fetch up to 8 days to cover the 7 days left reminder + 24h requirement
        List<Task> tasks = taskRepositoryPort.findTasksForReminders(upTo);

        for (Task task : tasks) {
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

    
}

package com.taskflow.infrastructure.web.controller;

import com.taskflow.infrastructure.scheduler.TaskReminderScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reminders")
public class ReminderController {

    private final TaskReminderScheduler taskReminderScheduler;

    public ReminderController(TaskReminderScheduler taskReminderScheduler) {
        this.taskReminderScheduler = taskReminderScheduler;
    }

    @PostMapping("/run")
    public ResponseEntity<?> runReminders() {
        taskReminderScheduler.triggerManually();
        return ResponseEntity.ok().build();
    }
}

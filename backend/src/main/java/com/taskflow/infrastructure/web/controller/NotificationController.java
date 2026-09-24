package com.taskflow.infrastructure.web.controller;

import com.taskflow.application.port.in.GetNotificationsUseCase;
import com.taskflow.application.port.in.MarkNotificationReadUseCase;
import com.taskflow.domain.model.NotificationReceiver;
import com.taskflow.domain.model.UserNotification;
import com.taskflow.infrastructure.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final GetNotificationsUseCase getNotificationsUseCase;
    private final MarkNotificationReadUseCase markNotificationReadUseCase;

    public NotificationController(GetNotificationsUseCase getNotificationsUseCase, MarkNotificationReadUseCase markNotificationReadUseCase) {
        this.getNotificationsUseCase = getNotificationsUseCase;
        this.markNotificationReadUseCase = markNotificationReadUseCase;
    }

    @GetMapping
    public ResponseEntity<?> getNotifications(
            @RequestParam(required = false) Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long targetUserId = userId != null ? userId : userDetails.getId();
        
        // Users can only view their own notifications
        if (!targetUserId.equals(userDetails.getId())) {
            return ResponseEntity.status(403).build();
        }

        List<UserNotification> items = getNotificationsUseCase.getNotifications(targetUserId);
        return ResponseEntity.ok(Map.of("items", items));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            NotificationReceiver receiver = markNotificationReadUseCase.markAsRead(notificationId, userDetails.getId());
            return ResponseEntity.ok(receiver);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(403).build();
        }
    }
}

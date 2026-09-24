package com.taskflow.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.domain.model.Notification;
import com.taskflow.domain.model.NotificationReceiver;
import com.taskflow.domain.repository.NotificationRepositoryPort;
import com.taskflow.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private NotificationRepositoryPort notificationRepositoryPort;

    @Test
    void shouldGetNotificationsAndMarkAsRead() throws Exception {
        Long userId = 7L;
        String token = jwtService.generateToken(userId, "test@test.com", "MEMBER");

        Notification notification = new Notification(null, "TEST_TYPE", "Test Title", "Test Message", UUID.randomUUID(), Instant.now(), null);
        notification = notificationRepositoryPort.saveNotification(notification);

        NotificationReceiver receiver = new NotificationReceiver(null, notification.getId(), userId, false, null);
        receiver = notificationRepositoryPort.saveReceiver(receiver);

        // 1. Get notifications
        mockMvc.perform(get("/api/v1/notifications")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].id").value(receiver.getId()))
                .andExpect(jsonPath("$.items[0].read").value(false));

        // 2. Mark as read
        mockMvc.perform(patch("/api/v1/notifications/" + receiver.getId() + "/read")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRead").value(true));

        // 3. Verify it's marked as read
        mockMvc.perform(get("/api/v1/notifications")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].read").value(true));
    }
}

package com.taskflow.infrastructure.web.controller;

import com.taskflow.domain.model.Priority;
import com.taskflow.domain.model.Task;
import com.taskflow.domain.repository.TaskRepositoryPort;
import com.taskflow.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TaskRepositoryPort taskRepositoryPort;

    @Test
    void shouldGetBoardSummaryAndTeamReport() throws Exception {
        Long userId = 1L;
        String token = jwtService.generateToken(userId, "admin@test.com", "ADMIN");

        UUID boardId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        
        // Add tasks to repository
        Task task1 = new Task(UUID.randomUUID(), boardId, "T1", "Desc", 1L, 10L, Priority.HIGH, Instant.now().minus(1, java.time.temporal.ChronoUnit.DAYS), 1L, Instant.now(), Instant.now(), null, 0, null, false, null, null);
        Task task2 = new Task(UUID.randomUUID(), boardId, "T2", "Desc", 1L, 10L, Priority.HIGH, Instant.now().plus(1, java.time.temporal.ChronoUnit.DAYS), 1L, Instant.now(), Instant.now(), null, 1, null, true, "blocked", Instant.now());
        Task task3 = new Task(UUID.randomUUID(), boardId, "T3", "Desc", 2L, 10L, Priority.HIGH, Instant.now().minus(1, java.time.temporal.ChronoUnit.DAYS), 1L, Instant.now(), Instant.now(), null, 0, Instant.now(), false, null, null);

        taskRepositoryPort.save(task1);
        taskRepositoryPort.save(task2);
        taskRepositoryPort.save(task3);

        // 1. Board summary
        mockMvc.perform(get("/api/v1/reports/boards/" + boardId + "/summary")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").isNumber())
                .andExpect(jsonPath("$.completionRate").isNumber());

        // 2. Team report
        mockMvc.perform(get("/api/v1/reports/team/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").isNumber());
    }
}

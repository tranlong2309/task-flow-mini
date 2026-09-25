package com.taskflow.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.application.port.in.*;
import com.taskflow.domain.model.Priority;
import com.taskflow.domain.model.Task;
import com.taskflow.domain.repository.BoardColumnRepositoryPort;
import com.taskflow.infrastructure.security.CustomUserDetails;
import com.taskflow.infrastructure.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = TaskController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
public class TaskControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateTaskUseCase createTaskUseCase;
    @MockBean
    private UpdateTaskUseCase updateTaskUseCase;
    @MockBean
    private GetTaskUseCase getTaskUseCase;
    @MockBean
    private SearchTasksUseCase searchTasksUseCase;
    @MockBean
    private DeleteTaskUseCase deleteTaskUseCase;
    @MockBean
    private UpdateTaskStatusUseCase updateTaskStatusUseCase;
    @MockBean
    private MoveTaskUseCase moveTaskUseCase;
    @MockBean
    private BlockTaskUseCase blockTaskUseCase;
    @MockBean
    private UnblockTaskUseCase unblockTaskUseCase;
    @MockBean
    private BoardColumnRepositoryPort boardColumnRepositoryPort;

    @BeforeEach
    void setUp() {
        CustomUserDetails userDetails = new CustomUserDetails(1L, "lan@agency.vn", "password", Set.of());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testCreateTask_Success() throws Exception {
        UUID boardId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Task mockTask = new Task();
        mockTask.setId(taskId);
        mockTask.setBoardId(boardId);
        mockTask.setTitle("Test Task");
        mockTask.setDescription("Description");
        mockTask.setPriority(Priority.HIGH);
        mockTask.setCreatedAt(Instant.now());
        mockTask.setStatusColumnId(1L);
        mockTask.setCreatedBy(1L);
        when(createTaskUseCase.createTask(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(mockTask);

        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "boardId", boardId.toString(),
                        "title", "Test Task",
                        "description", "Description",
                        "priority", "HIGH",
                        "statusColumnId", 1L
                ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void testCreateTask_MissingInput_Returns400() throws Exception {
        // Mock UseCase throwing IllegalArgumentException for missing required data
        when(createTaskUseCase.createTask(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Title is required"));

        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "boardId", UUID.randomUUID().toString(),
                        "description", "Missing title",
                        "priority", "HIGH"
                ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.detail").value("Title is required"));
    }

    @Test
    void testCreateTask_InvalidStatusFormat_Returns400() throws Exception {
        // Priority.valueOf throws IllegalArgumentException for invalid enum string
        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "boardId", UUID.randomUUID().toString(),
                        "title", "Test Task",
                        "priority", "INVALID_PRIORITY"
                ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }
}

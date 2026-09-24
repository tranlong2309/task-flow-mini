package com.taskflow.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.infrastructure.persistence.entity.BoardEntity;
import com.taskflow.infrastructure.persistence.entity.BoardMemberEntity;
import com.taskflow.infrastructure.persistence.entity.RoleEntity;
import com.taskflow.infrastructure.persistence.entity.TaskEntity;
import com.taskflow.infrastructure.persistence.entity.TeamEntity;
import com.taskflow.infrastructure.persistence.entity.UserEntity;
import com.taskflow.infrastructure.persistence.repository.SpringDataBoardMemberRepository;
import com.taskflow.infrastructure.persistence.repository.SpringDataBoardRepository;
import com.taskflow.infrastructure.persistence.repository.SpringDataRoleRepository;
import com.taskflow.infrastructure.persistence.repository.SpringDataTaskHistoryRepository;
import com.taskflow.infrastructure.persistence.repository.SpringDataTaskRepository;
import com.taskflow.infrastructure.persistence.repository.SpringDataTeamRepository;
import com.taskflow.infrastructure.persistence.repository.SpringDataUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.taskflow.infrastructure.persistence.entity.BoardColumnEntity;
import com.taskflow.infrastructure.persistence.repository.SpringDataBoardColumnRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataUserRepository userRepository;

    @Autowired
    private SpringDataBoardRepository boardRepository;

    @Autowired
    private SpringDataBoardColumnRepository boardColumnRepository;

    @Autowired
    private SpringDataBoardMemberRepository boardMemberRepository;

    @Autowired
    private SpringDataRoleRepository roleRepository;

    @Autowired
    private SpringDataTeamRepository teamRepository;

    @Autowired
    private SpringDataTaskRepository taskRepository;

    @Autowired
    private SpringDataTaskHistoryRepository taskHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;
    private UUID boardId;
    private Long userId;

    @BeforeEach
    void setUp() throws Exception {
        taskHistoryRepository.deleteAll();
        taskRepository.deleteAll();
        boardColumnRepository.deleteAll();
        boardMemberRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        teamRepository.deleteAll();

        RoleEntity role = new RoleEntity();
        role.setName("MEMBER");
        role = roleRepository.save(role);

        TeamEntity team = new TeamEntity();
        team.setName("Team A");
        team = teamRepository.save(team);

        UserEntity user = new UserEntity();
        user.setName("Lan");
        user.setEmail("lan@agency.vn");
        user.setPassword(passwordEncoder.encode("password"));
        user.setCreatedAt(Instant.now());
        user.setRoles(Set.of(role));
        user.setTeams(Set.of(team));
        user = userRepository.save(user);
        userId = user.getId();

        BoardEntity board = new BoardEntity();
        board.setId(UUID.randomUUID());
        board.setName("Board A");
        board.setTeamId(team.getId());
        board.setCreatedAt(Instant.now());
        board.setUpdatedAt(Instant.now());
        board = boardRepository.save(board);
        boardId = board.getId();

        BoardColumnEntity todoCol = new BoardColumnEntity();
        todoCol.setBoardId(boardId);
        todoCol.setName("Todo");
        todoCol.setPosition(0);
        boardColumnRepository.save(todoCol);

        BoardColumnEntity doneCol = new BoardColumnEntity();
        doneCol.setBoardId(boardId);
        doneCol.setName("Done");
        doneCol.setPosition(1);
        boardColumnRepository.save(doneCol);

        BoardMemberEntity member = new BoardMemberEntity();
        member.setBoardId(board.getId());
        member.setUserId(user.getId());
        member.setRoleName("MANAGER");
        boardMemberRepository.save(member);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "email", "lan@agency.vn",
                        "password", "password"
                ))))
                .andReturn();
        userToken = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void testTaskCrud() throws Exception {
        // 1. Create Task
        MvcResult createResult = mockMvc.perform(post("/api/v1/tasks")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "boardId", boardId.toString(),
                        "title", "Design landing page",
                        "description", "Create hero section and CTA",
                        "assigneeId", userId,
                        "priority", "HIGH",
                        "dueDate", "2026-09-30T17:00:00Z"
                ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Design landing page"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.assigneeId").value(userId))
                .andReturn();

        String taskIdStr = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();
        UUID taskId = UUID.fromString(taskIdStr);

        // 2. Get Task
        mockMvc.perform(get("/api/v1/tasks/" + taskId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Design landing page"));

        // 3. Update Task (Trigger history for assigneeId)
        mockMvc.perform(put("/api/v1/tasks/" + taskId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "title", "Updated title",
                        "priority", "MEDIUM",
                        "assigneeId", userId
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));

        // Verify task history recorded
        assertEquals(1, taskHistoryRepository.count());

        // 4. Search Tasks
        mockMvc.perform(get("/api/v1/boards/" + boardId + "/tasks")
                .header("Authorization", "Bearer " + userToken)
                .param("priority", "MEDIUM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Updated title"));

        // 4.1. Update Status (Move to Done column)
        mockMvc.perform(patch("/api/v1/tasks/" + taskId + "/status")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "statusColumnId", 2L,
                        "note", "Task is finished"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusColumnId").value(2))
                .andExpect(jsonPath("$.completedAt").isNotEmpty());

        // Verify task history recorded for status change + note
        assertEquals(3, taskHistoryRepository.count()); // 1 for assignee, 1 for status, 1 for note

        // 4.2. Drag and drop task (Move within column)
        mockMvc.perform(patch("/api/v1/tasks/" + taskId + "/move")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "sourceColumnId", 2L,
                        "targetColumnId", 2L,
                        "sourceIndex", 0,
                        "targetIndex", 1
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value(1));

        // 4.3. Block task
        mockMvc.perform(patch("/api/v1/tasks/" + taskId + "/block")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "reason", "Waiting for design approval"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isBlocked").value(true))
                .andExpect(jsonPath("$.blockedReason").value("Waiting for design approval"))
                .andExpect(jsonPath("$.completedAt").isEmpty()); // Should clear completedAt if it was Done

        // 4.4. Unblock task
        mockMvc.perform(patch("/api/v1/tasks/" + taskId + "/unblock")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isBlocked").value(false))
                .andExpect(jsonPath("$.blockedReason").isEmpty())
                .andExpect(jsonPath("$.completedAt").isNotEmpty()); // Should restore completedAt because it's in Done

        // 6. Delete Task (Soft delete)
        mockMvc.perform(delete("/api/v1/tasks/" + taskId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        // Ensure it doesn't show up in get Task
        mockMvc.perform(get("/api/v1/tasks/" + taskId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest());
                
        // 7. Workload summary
        mockMvc.perform(get("/api/v1/boards/" + boardId + "/workload")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.total").isNumber());
    }
}

package com.taskflow.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.infrastructure.persistence.entity.BoardEntity;
import com.taskflow.infrastructure.persistence.entity.BoardMemberEntity;
import com.taskflow.infrastructure.persistence.entity.RoleEntity;
import com.taskflow.infrastructure.persistence.entity.TeamEntity;
import com.taskflow.infrastructure.persistence.entity.UserEntity;
import com.taskflow.infrastructure.persistence.repository.SpringDataBoardMemberRepository;
import com.taskflow.infrastructure.persistence.repository.SpringDataBoardRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataUserRepository userRepository;

    @Autowired
    private SpringDataBoardRepository boardRepository;

    @Autowired
    private SpringDataBoardMemberRepository boardMemberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringDataRoleRepository roleRepository;

    @Autowired
    private SpringDataTeamRepository teamRepository;
    
    private UUID boardId;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        boardMemberRepository.deleteAll();
        boardRepository.deleteAll();
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

        BoardEntity board = new BoardEntity();
        board.setId(UUID.randomUUID());
        board.setName("Board A");
        board.setTeamId(team.getId());
        board.setCreatedAt(Instant.now());
        board.setUpdatedAt(Instant.now());
        board = boardRepository.save(board);
        boardId = board.getId();

        BoardMemberEntity member = new BoardMemberEntity();
        member.setBoardId(board.getId());
        member.setUserId(user.getId());
        member.setRoleName("MEMBER");
        boardMemberRepository.save(member);
    }

    @Test
    void testLoginAndGetMe() throws Exception {
        // Login
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "email", "lan@agency.vn",
                        "password", "password"
                ))))
                .andExpect(status().isOk())
                .andReturn();

        String responseStr = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseStr).get("token").asText();

        // Get Me
        mockMvc.perform(get("/api/v1/users/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("lan@agency.vn"))
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andExpect(jsonPath("$.teams").isArray());

        // Get Permissions
        mockMvc.perform(get("/api/v1/boards/" + boardId + "/permissions")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canCreateTask").value(true))
                .andExpect(jsonPath("$.canDeleteBoard").value(false));
    }
}

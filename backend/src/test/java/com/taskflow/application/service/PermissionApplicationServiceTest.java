package com.taskflow.application.service;

import com.taskflow.domain.model.BoardPermission;
import com.taskflow.domain.model.User;
import com.taskflow.domain.repository.BoardMemberRepositoryPort;
import com.taskflow.domain.repository.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PermissionApplicationServiceTest {

    @Mock
    private BoardMemberRepositoryPort boardMemberRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private PermissionApplicationService permissionApplicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getPermissions_shouldReturnAllTrue_whenUserIsAdmin() {
        UUID boardId = UUID.randomUUID();
        Long userId = 1L;
        User adminUser = new User(userId, "Admin", "admin@agency.vn", "ADMIN", List.of());
        
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(adminUser));

        BoardPermission permission = permissionApplicationService.getPermissions(boardId, userId);

        assertTrue(permission.isCanCreateTask());
        assertTrue(permission.isCanEditTask());
        assertTrue(permission.isCanDeleteBoard());
        assertTrue(permission.isCanViewReport());
    }

    @Test
    void getPermissions_shouldReturnAllTrue_whenUserIsManager() {
        UUID boardId = UUID.randomUUID();
        Long userId = 2L;
        User managerUser = new User(userId, "Manager", "manager@agency.vn", "MEMBER", List.of());
        
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(managerUser));
        when(boardMemberRepositoryPort.getRoleInBoard(boardId, userId)).thenReturn(Optional.of("MANAGER"));

        BoardPermission permission = permissionApplicationService.getPermissions(boardId, userId);

        assertTrue(permission.isCanCreateTask());
        assertTrue(permission.isCanEditTask());
        assertTrue(permission.isCanDeleteBoard());
        assertTrue(permission.isCanViewReport());
    }

    @Test
    void getPermissions_shouldReturnPartialPermissions_whenUserIsMember() {
        UUID boardId = UUID.randomUUID();
        Long userId = 3L;
        User memberUser = new User(userId, "Member", "member@agency.vn", "MEMBER", List.of());
        
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(memberUser));
        when(boardMemberRepositoryPort.getRoleInBoard(boardId, userId)).thenReturn(Optional.of("MEMBER"));

        BoardPermission permission = permissionApplicationService.getPermissions(boardId, userId);

        assertTrue(permission.isCanCreateTask());
        assertTrue(permission.isCanEditTask());
        assertFalse(permission.isCanDeleteBoard());
        assertFalse(permission.isCanViewReport());
    }

    @Test
    void getPermissions_shouldThrowException_whenUserNotInBoard() {
        UUID boardId = UUID.randomUUID();
        Long userId = 4L;
        User memberUser = new User(userId, "Member", "member@agency.vn", "MEMBER", List.of());
        
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(memberUser));
        when(boardMemberRepositoryPort.getRoleInBoard(boardId, userId)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> {
            permissionApplicationService.getPermissions(boardId, userId);
        });
    }
}

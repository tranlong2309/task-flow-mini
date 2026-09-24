package com.taskflow.application.service;

import com.taskflow.application.port.in.GetBoardPermissionUseCase;
import com.taskflow.domain.model.BoardPermission;
import com.taskflow.domain.model.User;
import com.taskflow.domain.repository.BoardMemberRepositoryPort;
import com.taskflow.domain.repository.UserRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PermissionApplicationService implements GetBoardPermissionUseCase {

    private final BoardMemberRepositoryPort boardMemberRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public PermissionApplicationService(BoardMemberRepositoryPort boardMemberRepositoryPort, UserRepositoryPort userRepositoryPort) {
        this.boardMemberRepositoryPort = boardMemberRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public BoardPermission getPermissions(UUID boardId, Long userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if ("ADMIN".equals(user.getRole())) {
            return new BoardPermission(true, true, true, true);
        }

        String boardRole = boardMemberRepositoryPort.getRoleInBoard(boardId, userId)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("User not in board"));

        if ("MANAGER".equals(boardRole)) {
            return new BoardPermission(true, true, true, true);
        }

        if ("MEMBER".equals(boardRole)) {
            return new BoardPermission(true, true, false, false);
        }

        throw new org.springframework.security.access.AccessDeniedException("Invalid role");
    }
}

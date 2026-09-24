package com.taskflow.application.port.in;

import com.taskflow.domain.model.BoardPermission;
import java.util.UUID;

public interface GetBoardPermissionUseCase {
    BoardPermission getPermissions(UUID boardId, Long userId);
}

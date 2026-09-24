package com.taskflow.infrastructure.web.controller;

import com.taskflow.application.port.in.CreateBoardUseCase;
import com.taskflow.domain.model.Board;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class BoardController {

    private final CreateBoardUseCase createBoardUseCase;
    private final com.taskflow.application.port.in.GetBoardPermissionUseCase getBoardPermissionUseCase;
    private final com.taskflow.application.port.in.GetWorkloadUseCase getWorkloadUseCase;

    public BoardController(CreateBoardUseCase createBoardUseCase, 
                           com.taskflow.application.port.in.GetBoardPermissionUseCase getBoardPermissionUseCase,
                           com.taskflow.application.port.in.GetWorkloadUseCase getWorkloadUseCase) {
        this.createBoardUseCase = createBoardUseCase;
        this.getBoardPermissionUseCase = getBoardPermissionUseCase;
        this.getWorkloadUseCase = getWorkloadUseCase;
    }

    @PostMapping("/boards")
    public ResponseEntity<?> createBoard(@RequestBody Map<String, Object> payload) {
        String name = payload.get("name") != null ? payload.get("name").toString() : null;
        String description = payload.get("description") != null ? payload.get("description").toString() : "";
        Long teamId = payload.get("teamId") != null ? Long.valueOf(payload.get("teamId").toString()) : null;

        Board board = createBoardUseCase.createBoard(name, description, teamId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", board.getId(),
                "name", board.getName(),
                "description", board.getDescription(),
                "teamId", board.getTeamId(),
                "createdAt", board.getCreatedAt()
        ));
    }

    @GetMapping("/boards/{boardId}/permissions")
    public ResponseEntity<?> getPermissions(
            @PathVariable java.util.UUID boardId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.taskflow.infrastructure.security.CustomUserDetails userDetails
    ) {
        com.taskflow.domain.model.BoardPermission permission = getBoardPermissionUseCase.getPermissions(boardId, userDetails.getId());
        return ResponseEntity.ok(Map.of(
                "canCreateTask", permission.isCanCreateTask(),
                "canEditTask", permission.isCanEditTask(),
                "canDeleteBoard", permission.isCanDeleteBoard(),
                "canViewReport", permission.isCanViewReport()
        ));
    }

    @GetMapping("/boards/{boardId}/workload")
    public ResponseEntity<?> getWorkload(
            @PathVariable java.util.UUID boardId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.taskflow.infrastructure.security.CustomUserDetails userDetails
    ) {
        try {
            com.taskflow.domain.model.BoardWorkloadSummary workload = getWorkloadUseCase.getWorkload(boardId, userDetails.getId());
            return ResponseEntity.ok(workload);
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

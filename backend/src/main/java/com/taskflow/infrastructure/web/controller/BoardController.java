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
    private final com.taskflow.application.port.in.GetBoardUseCase getBoardUseCase;
    private final com.taskflow.application.port.in.GetBoardPermissionUseCase getBoardPermissionUseCase;
    private final com.taskflow.application.port.in.GetWorkloadUseCase getWorkloadUseCase;
    private final com.taskflow.application.port.in.UpdateBoardUseCase updateBoardUseCase;
    private final com.taskflow.application.port.in.DeleteBoardUseCase deleteBoardUseCase;
    private final com.taskflow.application.port.in.UpdateBoardColumnsUseCase updateBoardColumnsUseCase;

    public BoardController(CreateBoardUseCase createBoardUseCase, 
                           com.taskflow.application.port.in.GetBoardUseCase getBoardUseCase,
                           com.taskflow.application.port.in.GetBoardPermissionUseCase getBoardPermissionUseCase,
                           com.taskflow.application.port.in.GetWorkloadUseCase getWorkloadUseCase,
                           com.taskflow.application.port.in.UpdateBoardUseCase updateBoardUseCase,
                           com.taskflow.application.port.in.DeleteBoardUseCase deleteBoardUseCase,
                           com.taskflow.application.port.in.UpdateBoardColumnsUseCase updateBoardColumnsUseCase) {
        this.createBoardUseCase = createBoardUseCase;
        this.getBoardUseCase = getBoardUseCase;
        this.getBoardPermissionUseCase = getBoardPermissionUseCase;
        this.getWorkloadUseCase = getWorkloadUseCase;
        this.updateBoardUseCase = updateBoardUseCase;
        this.deleteBoardUseCase = deleteBoardUseCase;
        this.updateBoardColumnsUseCase = updateBoardColumnsUseCase;
    }

    @PostMapping("/boards")
    public ResponseEntity<?> createBoard(@RequestBody Map<String, Object> payload,
                                         @org.springframework.security.core.annotation.AuthenticationPrincipal com.taskflow.infrastructure.security.CustomUserDetails userDetails) {
        String name = payload.get("name") != null ? payload.get("name").toString() : null;
        String description = payload.get("description") != null ? payload.get("description").toString() : "";
        Long teamId = payload.get("teamId") != null ? Long.valueOf(payload.get("teamId").toString()) : null;

        Board board = createBoardUseCase.createBoard(name, description, teamId, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", board.getId(),
                "name", board.getName(),
                "description", board.getDescription(),
                "teamId", board.getTeamId(),
                "createdAt", board.getCreatedAt()
        ));
    }

    @PutMapping("/boards/{boardId}")
    public ResponseEntity<?> updateBoard(@PathVariable java.util.UUID boardId, @RequestBody Map<String, Object> payload) {
        String name = payload.get("name") != null ? payload.get("name").toString() : null;
        String description = payload.get("description") != null ? payload.get("description").toString() : null;

        Board board = updateBoardUseCase.updateBoard(boardId, name, description);
        return ResponseEntity.ok(Map.of(
                "id", board.getId(),
                "name", board.getName(),
                "description", board.getDescription(),
                "teamId", board.getTeamId(),
                "createdAt", board.getCreatedAt()
        ));
    }

    @DeleteMapping("/boards/{boardId}")
    public ResponseEntity<?> deleteBoard(@PathVariable java.util.UUID boardId) {
        deleteBoardUseCase.deleteBoard(boardId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/boards/{boardId}/columns")
    public ResponseEntity<?> updateBoardColumns(@PathVariable java.util.UUID boardId, @RequestBody java.util.List<Map<String, Object>> payload) {
        java.util.List<com.taskflow.domain.model.BoardColumn> columns = payload.stream().map(c -> {
            String name = c.get("name") != null ? c.get("name").toString() : "";
            int position = c.get("position") != null ? Integer.parseInt(c.get("position").toString()) : 0;
            String tone = c.get("tone") != null ? c.get("tone").toString() : "gray";
            return new com.taskflow.domain.model.BoardColumn(null, boardId, name, position, tone);
        }).collect(java.util.stream.Collectors.toList());

        java.util.List<com.taskflow.domain.model.BoardColumn> saved = updateBoardColumnsUseCase.updateBoardColumns(boardId, columns);
        return ResponseEntity.ok(saved.stream().map(c -> Map.of(
            "id", c.getId(),
            "name", c.getName(),
            "order", c.getPosition(),
            "tone", c.getTone() != null ? c.getTone() : "gray"
        )).collect(java.util.stream.Collectors.toList()));
    }

    @GetMapping("/boards")
    public ResponseEntity<?> getBoards(@RequestParam(required = false, defaultValue = "1") Long teamId) {
        java.util.List<Board> boards = getBoardUseCase.getBoardsByTeamId(teamId);
        return ResponseEntity.ok(Map.of("data", boards.stream().map(board -> Map.of(
            "id", board.getId(),
            "boardId", board.getId(),
            "name", board.getName(),
            "description", board.getDescription(),
            "color", "blue"
        )).collect(java.util.stream.Collectors.toList())));
    }

    @GetMapping("/boards/{boardId}")
    public ResponseEntity<?> getBoard(@PathVariable java.util.UUID boardId) {
        Board board = getBoardUseCase.getBoard(boardId);
        java.util.List<com.taskflow.domain.model.BoardColumn> columns = getBoardUseCase.getBoardColumns(boardId);
        
        java.util.List<Map<String, Object>> columnsList = columns.stream()
            .map(c -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", c.getId());
                map.put("name", c.getName());
                map.put("order", c.getPosition());
                map.put("tone", c.getTone() != null ? c.getTone() : "gray");
                return map;
            })
            .collect(java.util.stream.Collectors.toList());

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", board.getId());
        response.put("name", board.getName());
        response.put("description", board.getDescription());
        response.put("teamId", board.getTeamId());
        response.put("columns", columnsList);
        response.put("createdAt", board.getCreatedAt());

        return ResponseEntity.ok(response);
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

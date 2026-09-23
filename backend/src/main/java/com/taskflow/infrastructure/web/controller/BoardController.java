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

    public BoardController(CreateBoardUseCase createBoardUseCase) {
        this.createBoardUseCase = createBoardUseCase;
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
}

package com.taskflow.domain.model;

import java.time.Instant;
import java.util.UUID;

public class BoardColumn {
    private Long id;
    private UUID boardId;
    private String name;
    private Integer position;

    public BoardColumn(Long id, UUID boardId, String name, Integer position) {
        this.id = id;
        this.boardId = boardId;
        this.name = name;
        this.position = position;
    }

    public Long getId() { return id; }
    public UUID getBoardId() { return boardId; }
    public String getName() { return name; }
    public Integer getPosition() { return position; }
}

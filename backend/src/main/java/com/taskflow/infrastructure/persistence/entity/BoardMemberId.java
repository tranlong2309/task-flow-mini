package com.taskflow.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class BoardMemberId implements Serializable {
    private UUID boardId;
    private Long userId;

    public BoardMemberId() {}

    public BoardMemberId(UUID boardId, Long userId) {
        this.boardId = boardId;
        this.userId = userId;
    }

    public UUID getBoardId() { return boardId; }
    public void setBoardId(UUID boardId) { this.boardId = boardId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardMemberId that = (BoardMemberId) o;
        return Objects.equals(boardId, that.boardId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boardId, userId);
    }
}

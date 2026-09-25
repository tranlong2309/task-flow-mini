package com.taskflow.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "board_members")
@IdClass(BoardMemberId.class)
public class BoardMemberEntity {

    @Id
    @Column(name = "board_id")
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.BINARY)
    private UUID boardId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "role_name", nullable = false)
    private String roleName;

    public BoardMemberEntity() {}
    
    public BoardMemberEntity(UUID boardId, Long userId, String roleName) {
        this.boardId = boardId;
        this.userId = userId;
        this.roleName = roleName;
    }

    public UUID getBoardId() { return boardId; }
    public void setBoardId(UUID boardId) { this.boardId = boardId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
}

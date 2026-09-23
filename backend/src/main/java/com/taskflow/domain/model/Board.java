package com.taskflow.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Board {
    private final UUID id;
    private String name;
    private String description;
    private Long teamId;
    private Instant createdAt;
    private Instant updatedAt;

    public Board(UUID id, String name, String description, Long teamId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.teamId = teamId;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

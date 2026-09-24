package com.taskflow.domain.model;

import java.util.List;

public class User {
    private Long id;
    private String name;
    private String email;
    private String role;
    private List<Long> teams;

    public User(Long id, String name, String email, String role, List<Long> teams) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.teams = teams;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public List<Long> getTeams() { return teams; }
}

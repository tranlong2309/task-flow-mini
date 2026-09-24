package com.taskflow.infrastructure.web.controller;

import com.taskflow.application.port.in.GetUserInfoUseCase;
import com.taskflow.domain.model.User;
import com.taskflow.infrastructure.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final GetUserInfoUseCase getUserInfoUseCase;
    private final com.taskflow.application.port.in.CreateUserUseCase createUserUseCase;
    private final com.taskflow.application.port.in.UpdateUserUseCase updateUserUseCase;
    private final com.taskflow.application.port.in.DeleteUserUseCase deleteUserUseCase;

    public UserController(GetUserInfoUseCase getUserInfoUseCase, 
                          com.taskflow.application.port.in.CreateUserUseCase createUserUseCase,
                          com.taskflow.application.port.in.UpdateUserUseCase updateUserUseCase,
                          com.taskflow.application.port.in.DeleteUserUseCase deleteUserUseCase) {
        this.getUserInfoUseCase = getUserInfoUseCase;
        this.createUserUseCase = createUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
    }

    @org.springframework.web.bind.annotation.PostMapping
    public ResponseEntity<?> createUser(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> payload) {
        String name = payload.get("name") != null ? payload.get("name").toString() : null;
        String email = payload.get("email") != null ? payload.get("email").toString() : null;
        String role = payload.get("role") != null ? payload.get("role").toString() : "MEMBER";

        User user = createUserUseCase.createUser(name, email, role);
        return ResponseEntity.ok(user);
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@org.springframework.web.bind.annotation.PathVariable Long id, @org.springframework.web.bind.annotation.RequestBody Map<String, Object> payload) {
        String name = payload.get("name") != null ? payload.get("name").toString() : null;
        String email = payload.get("email") != null ? payload.get("email").toString() : null;
        String role = payload.get("role") != null ? payload.get("role").toString() : null;

        User user = updateUserUseCase.updateUser(id, name, email, role);
        return ResponseEntity.ok(user);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@org.springframework.web.bind.annotation.PathVariable Long id) {
        deleteUserUseCase.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = getUserInfoUseCase.getCurrentUser(userDetails.getId());
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "teams", user.getTeams()
        ));
    }
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        java.util.List<User> users = getUserInfoUseCase.getAllUsers();
        java.util.List<Map<String, Object>> mappedUsers = users.stream()
            .map(user -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", user.getId());
                map.put("name", user.getName());
                map.put("email", user.getEmail());
                map.put("role", user.getRole());
                map.put("initials", user.getName().substring(0, Math.min(2, user.getName().length())).toUpperCase());
                map.put("color", "gray");
                return map;
            })
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(java.util.Collections.singletonMap("data", mappedUsers));
    }
}

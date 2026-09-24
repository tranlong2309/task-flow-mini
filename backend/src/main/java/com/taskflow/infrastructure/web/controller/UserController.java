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

    public UserController(GetUserInfoUseCase getUserInfoUseCase) {
        this.getUserInfoUseCase = getUserInfoUseCase;
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
}

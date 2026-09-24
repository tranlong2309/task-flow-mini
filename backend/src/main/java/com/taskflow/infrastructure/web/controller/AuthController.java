package com.taskflow.infrastructure.web.controller;

import com.taskflow.infrastructure.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final com.taskflow.application.port.in.GetUserInfoUseCase getUserInfoUseCase;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder, com.taskflow.application.port.in.GetUserInfoUseCase getUserInfoUseCase) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.getUserInfoUseCase = getUserInfoUseCase;
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            com.taskflow.infrastructure.security.CustomUserDetails userDetails = (com.taskflow.infrastructure.security.CustomUserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);
            
            com.taskflow.domain.model.User user = getUserInfoUseCase.getCurrentUser(userDetails.getId());
            Map<String, Object> userMap = Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "initials", user.getName().substring(0, Math.min(2, user.getName().length())).toUpperCase(),
                "color", "blue"
            );

            return ResponseEntity.ok(Map.of("token", token, "user", userMap));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }
    }
}

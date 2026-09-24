package com.taskflow.application.service;

import com.taskflow.application.port.in.GetUserInfoUseCase;
import com.taskflow.domain.model.User;
import com.taskflow.domain.repository.UserRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class UserApplicationService implements GetUserInfoUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public UserApplicationService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public User getCurrentUser(Long userId) {
        return userRepositoryPort.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }
}

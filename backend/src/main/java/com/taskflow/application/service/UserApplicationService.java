package com.taskflow.application.service;

import com.taskflow.application.port.in.GetUserInfoUseCase;
import com.taskflow.domain.model.User;
import com.taskflow.domain.repository.UserRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class UserApplicationService implements GetUserInfoUseCase, com.taskflow.application.port.in.CreateUserUseCase, com.taskflow.application.port.in.UpdateUserUseCase, com.taskflow.application.port.in.DeleteUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public UserApplicationService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public User getCurrentUser(Long userId) {
        return userRepositoryPort.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    @Override
    public java.util.List<User> getAllUsers() {
        return userRepositoryPort.findAll();
    }

    @Override
    public User createUser(String name, String email, String role) {
        User user = new User(null, name, email, role, java.util.Collections.emptyList());
        return userRepositoryPort.save(user);
    }

    @Override
    public User updateUser(Long id, String name, String email, String role) {
        User user = userRepositoryPort.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (name != null) user.setName(name);
        if (email != null) user.setEmail(email);
        if (role != null) user.setRole(role);
        return userRepositoryPort.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepositoryPort.deleteById(id);
    }
}

package com.taskflow.application.port.in;

import com.taskflow.domain.model.User;

public interface UpdateUserUseCase {
    User updateUser(Long id, String name, String email, String role);
}

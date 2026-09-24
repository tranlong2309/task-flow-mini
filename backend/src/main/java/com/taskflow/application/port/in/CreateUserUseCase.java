package com.taskflow.application.port.in;

import com.taskflow.domain.model.User;

public interface CreateUserUseCase {
    User createUser(String name, String email, String role);
}

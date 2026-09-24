package com.taskflow.domain.repository;

import com.taskflow.domain.model.User;
import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findById(Long id);
}

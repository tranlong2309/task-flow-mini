package com.taskflow.domain.repository;

import com.taskflow.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findById(Long id);
    List<User> findByIds(List<Long> ids);
    List<User> findAll();
    User save(User user);
    void deleteById(Long id);
}

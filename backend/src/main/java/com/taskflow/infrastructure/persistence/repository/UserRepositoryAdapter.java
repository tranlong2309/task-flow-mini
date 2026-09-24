package com.taskflow.infrastructure.persistence.repository;

import com.taskflow.domain.model.User;
import com.taskflow.domain.repository.UserRepositoryPort;
import com.taskflow.infrastructure.persistence.entity.RoleEntity;
import com.taskflow.infrastructure.persistence.entity.TeamEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository springDataUserRepository;

    public UserRepositoryAdapter(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public Optional<User> findById(Long id) {
        return springDataUserRepository.findById(id).map(entity -> {
            String role = entity.getRoles().stream()
                    .map(RoleEntity::getName)
                    .findFirst()
                    .orElse("MEMBER");
            List<Long> teams = entity.getTeams().stream()
                    .map(TeamEntity::getId)
                    .collect(Collectors.toList());

            return new User(entity.getId(), entity.getName(), entity.getEmail(), role, teams);
        });
    }
}

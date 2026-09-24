package com.taskflow.infrastructure.persistence.repository;

import com.taskflow.domain.model.User;
import com.taskflow.domain.repository.UserRepositoryPort;
import com.taskflow.infrastructure.persistence.entity.RoleEntity;
import com.taskflow.infrastructure.persistence.entity.TeamEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = true)
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
    @Override
    @Transactional(readOnly = true)
    public List<User> findByIds(List<Long> ids) {
        return springDataUserRepository.findAllById(ids).stream().map(entity -> {
            String role = entity.getRoles().stream()
                    .map(RoleEntity::getName)
                    .findFirst()
                    .orElse("MEMBER");
            List<Long> teams = entity.getTeams().stream()
                    .map(TeamEntity::getId)
                    .collect(Collectors.toList());

            return new User(entity.getId(), entity.getName(), entity.getEmail(), role, teams);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return springDataUserRepository.findAll().stream().map(entity -> {
            String role = entity.getRoles().stream()
                    .map(RoleEntity::getName)
                    .findFirst()
                    .orElse("MEMBER");
            List<Long> teams = entity.getTeams().stream()
                    .map(TeamEntity::getId)
                    .collect(Collectors.toList());

            return new User(entity.getId(), entity.getName(), entity.getEmail(), role, teams);
        }).collect(Collectors.toList());
    }
    @Override
    @Transactional
    public User save(User user) {
        com.taskflow.infrastructure.persistence.entity.UserEntity entity = new com.taskflow.infrastructure.persistence.entity.UserEntity();
        if (user.getId() != null) {
            entity = springDataUserRepository.findById(user.getId()).orElse(entity);
        }
        entity.setName(user.getName());
        entity.setEmail(user.getEmail());
        if (entity.getPassword() == null) {
            entity.setPassword(""); // dummy password
        }
        entity.setCreatedAt(java.time.Instant.now());
        springDataUserRepository.save(entity);
        return user;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        springDataUserRepository.deleteById(id);
    }
}

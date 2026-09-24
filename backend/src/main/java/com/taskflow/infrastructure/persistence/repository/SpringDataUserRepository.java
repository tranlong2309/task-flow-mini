package com.taskflow.infrastructure.persistence.repository;

import com.taskflow.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserEntity, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"roles", "teams"})
    Optional<UserEntity> findByEmail(String email);
}

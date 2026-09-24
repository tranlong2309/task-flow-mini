package com.taskflow.infrastructure.persistence.repository;

import com.taskflow.infrastructure.persistence.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataTeamRepository extends JpaRepository<TeamEntity, Long> {
}

package com.taskflow.infrastructure.persistence.repository;

import com.taskflow.domain.model.Priority;
import com.taskflow.domain.model.Task;
import com.taskflow.domain.repository.TaskRepositoryPort;
import com.taskflow.infrastructure.persistence.entity.TaskEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import jakarta.persistence.criteria.Predicate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TaskRepositoryAdapter implements TaskRepositoryPort {

    private final SpringDataTaskRepository springDataTaskRepository;

    public TaskRepositoryAdapter(SpringDataTaskRepository springDataTaskRepository) {
        this.springDataTaskRepository = springDataTaskRepository;
    }

    @Override
    public Task save(Task task) {
        TaskEntity entity = toEntity(task);
        TaskEntity saved = springDataTaskRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Task> findById(UUID id) {
        return springDataTaskRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Task> searchTasks(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly) {
        Specification<TaskEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("boardId"), boardId));
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (statusColumnId != null) {
                predicates.add(cb.equal(root.get("statusColumnId"), statusColumnId));
            }
            if (assigneeId != null) {
                predicates.add(cb.equal(root.get("assigneeId"), assigneeId));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }
            if (Boolean.TRUE.equals(overdueOnly)) {
                predicates.add(cb.lessThan(root.get("dueDate"), Instant.now()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return springDataTaskRepository.findAll(spec).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private TaskEntity toEntity(Task task) {
        TaskEntity entity = new TaskEntity();
        entity.setId(task.getId());
        entity.setBoardId(task.getBoardId());
        entity.setTitle(task.getTitle());
        entity.setDescription(task.getDescription());
        entity.setStatusColumnId(task.getStatusColumnId());
        entity.setAssigneeId(task.getAssigneeId());
        entity.setPriority(task.getPriority());
        entity.setDueDate(task.getDueDate());
        entity.setCreatedBy(task.getCreatedBy());
        entity.setCreatedAt(task.getCreatedAt());
        entity.setUpdatedAt(task.getUpdatedAt());
        entity.setDeletedAt(task.getDeletedAt());
        return entity;
    }

    private Task toDomain(TaskEntity entity) {
        return new Task(
                entity.getId(),
                entity.getBoardId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatusColumnId(),
                entity.getAssigneeId(),
                entity.getPriority(),
                entity.getDueDate(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}

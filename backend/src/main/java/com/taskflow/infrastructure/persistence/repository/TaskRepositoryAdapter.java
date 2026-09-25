package com.taskflow.infrastructure.persistence.repository;

import com.taskflow.domain.model.Priority;
import com.taskflow.domain.model.Task;
import com.taskflow.domain.repository.TaskRepositoryPort;
import com.taskflow.infrastructure.web.dto.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public java.util.List<Task> findTasksForReminders(java.time.Instant upTo) {
        return springDataTaskRepository.findTasksForReminders(upTo).stream().map(this::toDomain).collect(java.util.stream.Collectors.toList());
    }

    public Task save(Task task) {
        TaskEntity entity = toEntity(task);
        TaskEntity saved = springDataTaskRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void saveAll(List<Task> tasks) {
        List<TaskEntity> entities = tasks.stream().map(this::toEntity).collect(Collectors.toList());
        springDataTaskRepository.saveAll(entities);
    }

    @Override
    public Optional<Task> findById(UUID id) {
        return springDataTaskRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Task> findByStatusColumnIdOrderByPositionAsc(Long statusColumnId) {
        return springDataTaskRepository.findByStatusColumnIdOrderByPositionAsc(statusColumnId)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public PagedResponse<Task> searchTasks(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly, 
                                           Instant assignedDateFrom, Instant assignedDateTo, 
                                           Instant startDateFrom, Instant startDateTo, 
                                           Instant endDateFrom, Instant endDateTo, 
                                           int page, int size) {
        Specification<TaskEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("boardId"), boardId));
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (statusColumnId != null) {
                predicates.add(cb.equal(root.get("statusColumnId"), statusColumnId));
            }
            if (assigneeId != null) {
                predicates.add(cb.equal(cb.function("JSON_CONTAINS", Integer.class, root.get("assigneeIds"), cb.literal(assigneeId.toString())), 1));
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
                predicates.add(cb.isNull(root.get("completedAt")));
            }
            
            // Map Assigned Date and Start Date to createdAt, End Date to dueDate
            if (assignedDateFrom != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), assignedDateFrom));
            if (assignedDateTo != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), assignedDateTo));
            if (startDateFrom != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDateFrom));
            if (startDateTo != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), startDateTo));
            if (endDateFrom != null) predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), endDateFrom));
            if (endDateTo != null) predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), endDateTo));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<TaskEntity> entityPage = springDataTaskRepository.findAll(spec, pageRequest);
        java.util.List<Task> tasks = entityPage.getContent().stream().map(this::toDomain).collect(Collectors.toList());
        return new PagedResponse<>(tasks, entityPage.getTotalElements(), entityPage.getTotalPages(), page, size);
    }

    
    @Override
    public java.util.List<Task> searchTasksByBoardIds(java.util.List<UUID> boardIds, Long assigneeId, java.time.Instant from, java.time.Instant to) {
        Specification<TaskEntity> spec = (root, query, cb) -> {
            java.util.List<Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(root.get("boardId").in(boardIds));
            predicates.add(cb.isNull(root.get("deletedAt")));
            if (assigneeId != null) {
                predicates.add(cb.equal(cb.function("JSON_CONTAINS", Integer.class, root.get("assigneeIds"), cb.literal(assigneeId.toString())), 1));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return springDataTaskRepository.findAll(spec).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countByBoardId(UUID boardId) {
        return springDataTaskRepository.countByBoardIdAndDeletedAtIsNull(boardId);
    }

    @Override
    public long countByBoardIdAndCompletedAtIsNotNull(UUID boardId) {
        return springDataTaskRepository.countByBoardIdAndCompletedAtIsNotNullAndDeletedAtIsNull(boardId);
    }

    @Override
    public long countByBoardIdAndStatusColumnId(UUID boardId, Long statusColumnId) {
        return springDataTaskRepository.countByBoardIdAndStatusColumnIdAndDeletedAtIsNull(boardId, statusColumnId);
    }

    @Override
    public long countByBoardIdAndIsBlockedTrue(UUID boardId) {
        return springDataTaskRepository.countByBoardIdAndIsBlockedTrueAndDeletedAtIsNull(boardId);
    }

    @Override
    public long countByBoardIdAndDueDateBeforeAndCompletedAtIsNull(UUID boardId, java.time.Instant date) {
        return springDataTaskRepository.countByBoardIdAndDueDateBeforeAndCompletedAtIsNullAndDeletedAtIsNull(boardId, date);
    }

    private TaskEntity toEntity(Task task) {
        TaskEntity entity = new TaskEntity();
        entity.setId(task.getId());
        entity.setBoardId(task.getBoardId());
        entity.setTitle(task.getTitle());
        entity.setDescription(task.getDescription());
        entity.setStatusColumnId(task.getStatusColumnId());
        entity.setAssigneeIds(task.getAssigneeIds());
        entity.setAssignedDate(task.getAssignedDate());
        entity.setStartDate(task.getStartDate());
        entity.setSubtasks(task.getSubtasks());
        entity.setComments(task.getComments());
        entity.setAttachments(task.getAttachments());
        entity.setPriority(task.getPriority());
        entity.setDueDate(task.getDueDate());
        entity.setCreatedBy(task.getCreatedBy());
        entity.setCreatedAt(task.getCreatedAt());
        entity.setUpdatedAt(task.getUpdatedAt());
        entity.setDeletedAt(task.getDeletedAt());
        entity.setPosition(task.getPosition() != null ? task.getPosition() : 0);
        entity.setCompletedAt(task.getCompletedAt());
        entity.setIsBlocked(task.getIsBlocked() != null ? task.getIsBlocked() : false);
        entity.setBlockedReason(task.getBlockedReason());
        entity.setBlockedAt(task.getBlockedAt());
        return entity;
    }

    private Task toDomain(TaskEntity entity) {
        return new Task(
                entity.getId(),
                entity.getBoardId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatusColumnId(),
                entity.getAssigneeIds(),
                entity.getAssignedDate(),
                entity.getStartDate(),
                entity.getSubtasks(),
                entity.getComments(),
                entity.getAttachments(),
                entity.getPriority(),
                entity.getDueDate(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getPosition(),
                entity.getCompletedAt(),
                entity.getIsBlocked(),
                entity.getBlockedReason(),
                entity.getBlockedAt()
        );
    }
}

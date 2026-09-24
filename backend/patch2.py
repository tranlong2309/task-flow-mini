import os
import re

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"

def patch(rel_path):
    path = os.path.join(base_dir, rel_path)
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Imports
    if "PagedResponse" not in content:
        content = content.replace("import com.taskflow.domain.repository.TaskRepositoryPort;", "import com.taskflow.domain.repository.TaskRepositoryPort;\nimport com.taskflow.infrastructure.web.dto.PagedResponse;\nimport org.springframework.data.domain.Page;\nimport org.springframework.data.domain.PageRequest;")

    # 2. searchTasks signature
    content = re.sub(
        r"public List<Task> searchTasks\(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly\) \{",
        r"public PagedResponse<Task> searchTasks(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly, int page, int size) {",
        content, count=1
    )

    # 3. end of searchTasks
    content = re.sub(
        r"return springDataTaskRepository\.findAll\(spec\)\.stream\(\)\s*\.map\(this::toDomain\)\s*\.collect\(Collectors\.toList\(\)\);",
        r"PageRequest pageRequest = PageRequest.of(page, size);\n        Page<TaskEntity> entityPage = springDataTaskRepository.findAll(spec, pageRequest);\n        java.util.List<Task> tasks = entityPage.getContent().stream().map(this::toDomain).collect(Collectors.toList());\n        return new PagedResponse<>(tasks, entityPage.getTotalElements(), entityPage.getTotalPages(), page, size);",
        content, count=1
    )

    # 4. new methods
    new_methods = '''
    @Override
    public java.util.List<Task> searchTasksByBoardIds(java.util.List<UUID> boardIds, Long assigneeId, java.time.Instant from, java.time.Instant to) {
        Specification<TaskEntity> spec = (root, query, cb) -> {
            java.util.List<Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(root.get("boardId").in(boardIds));
            predicates.add(cb.isNull(root.get("deletedAt")));
            if (assigneeId != null) {
                predicates.add(cb.equal(root.get("assigneeId"), assigneeId));
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
'''
    content = content.replace("private TaskEntity toEntity(Task task) {", new_methods + "\n    private TaskEntity toEntity(Task task) {")

    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Patched {rel_path}")

patch(r"infrastructure\persistence\repository\TaskRepositoryAdapter.java")

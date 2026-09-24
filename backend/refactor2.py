import os
import re

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"

def update_file(rel_path, pattern, replacement):
    path = os.path.join(base_dir, rel_path)
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    content, count = re.subn(pattern, replacement, content, flags=re.MULTILINE | re.DOTALL)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Updated {rel_path} ({count} changes)")

# TaskApplicationService
update_file(
    r"application\service\TaskApplicationService.java",
    r"public List<Task> searchTasks\(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly, Long requesterId\) \{\s*getBoardPermissionUseCase.getPermissions\(boardId, requesterId\);\s*return taskRepositoryPort.searchTasks\(boardId, statusColumnId, assigneeId, priority, search, overdueOnly\);\s*\}",
    r"import com.taskflow.infrastructure.web.dto.PagedResponse;\n\n    @Override\n    public PagedResponse<Task> searchTasks(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly, Long requesterId, int page, int size) {\n        getBoardPermissionUseCase.getPermissions(boardId, requesterId);\n        return taskRepositoryPort.searchTasks(boardId, statusColumnId, assigneeId, priority, search, overdueOnly, page, size);\n    }"
)

# TaskRepositoryAdapter
update_file(
    r"infrastructure\persistence\repository\TaskRepositoryAdapter.java",
    r"public List<Task> searchTasks\(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly\) \{",
    r"import com.taskflow.infrastructure.web.dto.PagedResponse;\nimport org.springframework.data.domain.Page;\nimport org.springframework.data.domain.PageRequest;\n\n    @Override\n    public PagedResponse<Task> searchTasks(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly, int page, int size) {"
)

update_file(
    r"infrastructure\persistence\repository\TaskRepositoryAdapter.java",
    r"return springDataTaskRepository\.findAll\(spec\)\.stream\(\)\s*\.map\(TaskEntityMapper::toDomain\)\s*\.toList\(\);",
    r"PageRequest pageRequest = PageRequest.of(page, size);\n        Page<TaskEntity> entityPage = springDataTaskRepository.findAll(spec, pageRequest);\n        List<Task> tasks = entityPage.getContent().stream().map(TaskEntityMapper::toDomain).toList();\n        return new PagedResponse<>(tasks, entityPage.getTotalElements(), entityPage.getTotalPages(), page, size);"
)

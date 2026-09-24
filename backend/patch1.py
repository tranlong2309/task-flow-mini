import os
import re

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"

def patch(rel_path, pattern, replacement, count=1):
    path = os.path.join(base_dir, rel_path)
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    new_content, n = re.subn(pattern, replacement, content, count=count, flags=re.MULTILINE | re.DOTALL)
    if n > 0:
        with open(path, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Patched {rel_path} ({n} times)")
    else:
        print(f"Failed to patch {rel_path} - pattern not found")

# Create PagedResponse if not exists
paged_path = os.path.join(base_dir, r"infrastructure\web\dto\PagedResponse.java")
if not os.path.exists(paged_path):
    os.makedirs(os.path.dirname(paged_path), exist_ok=True)
    with open(paged_path, 'w', encoding='utf-8') as f:
        f.write("package com.taskflow.infrastructure.web.dto;\n\nimport java.util.List;\n\npublic record PagedResponse<T>(\n    List<T> content,\n    long totalElements,\n    int totalPages,\n    int page,\n    int size\n) {}\n")
    print("Created PagedResponse.java")

# 1. Update TaskRepositoryPort
patch(r"domain\repository\TaskRepositoryPort.java",
    r"List<Task> searchTasks\(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly\);",
    r"import com.taskflow.infrastructure.web.dto.PagedResponse;\n\n    PagedResponse<Task> searchTasks(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly, int page, int size);\n    List<Task> searchTasksByBoardIds(List<UUID> boardIds, Long assigneeId, Instant from, Instant to);\n    long countByBoardId(UUID boardId);\n    long countByBoardIdAndStatusColumnId(UUID boardId, Long statusColumnId);\n    long countByBoardIdAndIsBlockedTrue(UUID boardId);\n    long countByBoardIdAndDueDateBeforeAndCompletedAtIsNull(UUID boardId, Instant date);")

# 2. Update SearchTasksUseCase
patch(r"application\port\in\SearchTasksUseCase.java",
    r"List<Task> searchTasks\(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly, Long requesterId\);",
    r"import com.taskflow.infrastructure.web.dto.PagedResponse;\n\n    PagedResponse<Task> searchTasks(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly, Long requesterId, int page, int size);")

# 3. Update TaskApplicationService
patch(r"application\service\TaskApplicationService.java",
    r"public List<Task> searchTasks\(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly, Long requesterId\) \{(.*?)\}",
    r"import com.taskflow.infrastructure.web.dto.PagedResponse;\n\n    @Override\n    public PagedResponse<Task> searchTasks(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly, Long requesterId, int page, int size) {\1\n        return taskRepositoryPort.searchTasks(boardId, statusColumnId, assigneeId, priority, search, overdueOnly, page, size);\n    }")

patch(r"application\service\TaskApplicationService.java",
    r"return taskRepositoryPort\.searchTasks\(boardId, statusColumnId, assigneeId, priority, search, overdueOnly\);",
    r"") # Clean up old return statement


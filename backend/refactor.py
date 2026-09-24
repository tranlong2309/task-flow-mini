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

# 1. PagedResponse usage in TaskRepositoryPort
update_file(
    r"domain\repository\TaskRepositoryPort.java",
    r"List<Task> searchTasks\(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly\);",
    r"import com.taskflow.infrastructure.web.dto.PagedResponse;\n\n    PagedResponse<Task> searchTasks(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly, int page, int size);"
)

# 2. PagedResponse usage in SearchTasksUseCase
update_file(
    r"application\port\in\SearchTasksUseCase.java",
    r"List<Task> searchTasks\(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly, Long requesterId\);",
    r"import com.taskflow.infrastructure.web.dto.PagedResponse;\n\n    PagedResponse<Task> searchTasks(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly, Long requesterId, int page, int size);"
)


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

update_file(
    r"infrastructure\persistence\repository\TaskRepositoryAdapter.java",
    r"return springDataTaskRepository\.findAll\(spec\)\.stream\(\)\s*\.map\(this::toDomain\)\s*\.collect\(Collectors\.toList\(\)\);",
    r"PageRequest pageRequest = PageRequest.of(page, size);\n        Page<TaskEntity> entityPage = springDataTaskRepository.findAll(spec, pageRequest);\n        java.util.List<Task> tasks = entityPage.getContent().stream().map(this::toDomain).collect(Collectors.toList());\n        return new PagedResponse<>(tasks, entityPage.getTotalElements(), entityPage.getTotalPages(), page, size);"
)

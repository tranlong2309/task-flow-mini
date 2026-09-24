import os
import re

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"

def fix_imports(rel_path):
    path = os.path.join(base_dir, rel_path)
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Remove bad imports injected randomly
    content = content.replace("import com.taskflow.infrastructure.web.dto.PagedResponse;\n\n    ", "    ")
    content = content.replace("import com.taskflow.infrastructure.web.dto.PagedResponse;\nimport org.springframework.data.domain.Page;\nimport org.springframework.data.domain.PageRequest;\n\n    ", "    ")
    
    # Add imports to top
    if "PagedResponse" not in content[:1000]:
        content = content.replace("package com.taskflow.", "package com.taskflow.\n\nimport com.taskflow.infrastructure.web.dto.PagedResponse;\n", 1)
        if "PageRequest" not in content and "Page<" in content:
            content = content.replace("package com.taskflow.\n\nimport", "package com.taskflow.\n\nimport org.springframework.data.domain.Page;\nimport org.springframework.data.domain.PageRequest;\nimport", 1)
            
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Fixed {rel_path}")

fix_imports(r"domain\repository\TaskRepositoryPort.java")
fix_imports(r"application\port\in\SearchTasksUseCase.java")
fix_imports(r"application\service\TaskApplicationService.java")
fix_imports(r"infrastructure\persistence\repository\TaskRepositoryAdapter.java")
fix_imports(r"domain\repository\NotificationRepositoryPort.java")
fix_imports(r"application\port\in\GetNotificationsUseCase.java")
fix_imports(r"application\service\NotificationApplicationService.java")
fix_imports(r"infrastructure\persistence\repository\NotificationRepositoryAdapter.java")


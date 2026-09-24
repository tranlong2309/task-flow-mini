import os
import re

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"

def fix_package(rel_path):
    path = os.path.join(base_dir, rel_path)
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Fix the broken package name. It looks like:
    # package com.taskflow.
    # 
    # import com.taskflow.infrastructure.web.dto.PagedResponse;
    # application.service;
    
    # Let's just fix it manually for each file.
    if "TaskApplicationService.java" in rel_path:
        content = content.replace("package com.taskflow.\n\nimport com.taskflow.infrastructure.web.dto.PagedResponse;\napplication.service;", "package com.taskflow.application.service;\n\nimport com.taskflow.infrastructure.web.dto.PagedResponse;")
    elif "NotificationRepositoryAdapter.java" in rel_path:
        content = content.replace("package com.taskflow.\n\nimport org.springframework.data.domain.Page;\nimport org.springframework.data.domain.PageRequest;\nimport com.taskflow.infrastructure.web.dto.PagedResponse;\ninfrastructure.persistence.repository;", "package com.taskflow.infrastructure.persistence.repository;\n\nimport org.springframework.data.domain.Page;\nimport org.springframework.data.domain.PageRequest;\nimport com.taskflow.infrastructure.web.dto.PagedResponse;")
    elif "TaskRepositoryAdapter.java" in rel_path:
        content = content.replace("package com.taskflow.\n\nimport org.springframework.data.domain.Page;\nimport org.springframework.data.domain.PageRequest;\nimport com.taskflow.infrastructure.web.dto.PagedResponse;\ninfrastructure.persistence.repository;", "package com.taskflow.infrastructure.persistence.repository;\n\nimport org.springframework.data.domain.Page;\nimport org.springframework.data.domain.PageRequest;\nimport com.taskflow.infrastructure.web.dto.PagedResponse;")
    
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

fix_package(r"application\service\TaskApplicationService.java")
fix_package(r"infrastructure\persistence\repository\TaskRepositoryAdapter.java")
fix_package(r"infrastructure\persistence\repository\NotificationRepositoryAdapter.java")


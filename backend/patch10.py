# -*- coding: utf-8 -*-
import os

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"

def patch(rel_path, replacements):
    path = os.path.join(base_dir, rel_path)
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    for old, new in replacements:
        content = content.replace(old, new)
        
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Patched {rel_path}")

patch(r"infrastructure\persistence\repository\BoardColumnRepositoryAdapter.java", [
    ("package com.taskflow.infrastructure.persistence.repository;", "package com.taskflow.infrastructure.persistence.repository;\n\nimport java.util.List;")
])

patch(r"infrastructure\web\controller\TaskController.java", [
    ("import com.taskflow.application.port.in.UpdateTaskUseCase;", "import com.taskflow.application.port.in.UpdateTaskUseCase;\nimport com.taskflow.application.port.in.UpdateTaskStatusUseCase;\nimport com.taskflow.application.port.in.MoveTaskUseCase;\nimport com.taskflow.application.port.in.BlockTaskUseCase;\nimport com.taskflow.application.port.in.UnblockTaskUseCase;")
])

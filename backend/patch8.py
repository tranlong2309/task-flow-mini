# -*- coding: utf-8 -*-
import os
import re

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"

def fix_imports(rel_path):
    path = os.path.join(base_dir, rel_path)
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Move import out of class body
    if "import com.taskflow.infrastructure.web.dto.PagedResponse;\n\n    PagedResponse" in content:
        content = content.replace("import com.taskflow.infrastructure.web.dto.PagedResponse;\n\n    PagedResponse", "    PagedResponse")
        # Add to top if not present
        if "import com.taskflow.infrastructure.web.dto.PagedResponse;" not in content:
            content = content.replace("package com.taskflow.", "package com.taskflow.", 1)
            content = re.sub(r"(package .*?;)", r"\1\n\nimport com.taskflow.infrastructure.web.dto.PagedResponse;", content, count=1)
            
    if "import com.taskflow.infrastructure.web.dto.PagedResponse;\n\n    @Override\n    public PagedResponse" in content:
        content = content.replace("import com.taskflow.infrastructure.web.dto.PagedResponse;\n\n    @Override\n    public PagedResponse", "    @Override\n    public PagedResponse")
        if "import com.taskflow.infrastructure.web.dto.PagedResponse;" not in content:
            content = re.sub(r"(package .*?;)", r"\1\n\nimport com.taskflow.infrastructure.web.dto.PagedResponse;", content, count=1)

    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Fixed imports in {rel_path}")

fix_imports(r"domain\repository\TaskRepositoryPort.java")
fix_imports(r"application\port\in\SearchTasksUseCase.java")
fix_imports(r"application\service\TaskApplicationService.java")


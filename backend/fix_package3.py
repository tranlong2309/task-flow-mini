import os

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"

def manual_fix(rel_path):
    path = os.path.join(base_dir, rel_path)
    with open(path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
        
    new_lines = []
    for line in lines:
        if line.startswith("package com.taskflow.infrastructure.persistence.repository;"):
            new_lines.append(line)
        elif line.startswith("package com.taskflow."):
            pass # ignore broken package line
        elif line.strip() == "infrastructure.persistence.repository;":
            pass # ignore the broken end of package line
        else:
            new_lines.append(line)
            
    # make sure package is present
    content = "".join(new_lines)
    if not content.startswith("package "):
        content = "package com.taskflow.infrastructure.persistence.repository;\n\n" + content
        
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

manual_fix(r"infrastructure\persistence\repository\TaskRepositoryAdapter.java")
manual_fix(r"infrastructure\persistence\repository\NotificationRepositoryAdapter.java")


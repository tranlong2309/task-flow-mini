import os
import re

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"

def fix_package_regex(rel_path):
    path = os.path.join(base_dir, rel_path)
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # regex to fix "package com.taskflow.\n\nimport... \n<rest of package>;"
    content = re.sub(r"package com\.taskflow\.\n\n(.*?)(\n[a-zA-Z0-9_\.]+;)", r"package com.taskflow\2\n\n\1", content, flags=re.DOTALL)
    
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

fix_package_regex(r"domain\repository\TaskRepositoryPort.java")
fix_package_regex(r"application\port\in\SearchTasksUseCase.java")
fix_package(r"domain\repository\NotificationRepositoryPort.java")
fix_package(r"application\port\in\GetNotificationsUseCase.java")
fix_package_regex(r"application\service\NotificationApplicationService.java")

# -*- coding: utf-8 -*-
import os

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\test\java\com\taskflow\infrastructure\web\controller"

def remove_jwt_import(file_name):
    path = os.path.join(base_dir, file_name)
    if os.path.exists(path):
        with open(path, 'r', encoding='utf-8') as f:
            content = f.read()
        content = content.replace("import com.taskflow.infrastructure.security.JwtService;", "")
        content = content.replace("@MockBean\n    private JwtService jwtService;", "")
        content = content.replace("@MockBean\n    private SpringDataRoleRepository roleRepository;", "")
        content = content.replace("@MockBean\n    private SpringDataTeamRepository teamRepository;", "")
        
        # for AuthIntegrationTest specifically
        content = content.replace("import com.taskflow.infrastructure.persistence.repository.SpringDataRoleRepository;", "")
        content = content.replace("import com.taskflow.infrastructure.persistence.repository.SpringDataTeamRepository;", "")
        
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Patched {file_name}")

remove_jwt_import("NotificationIntegrationTest.java")
remove_jwt_import("ReportIntegrationTest.java")
remove_jwt_import("AuthIntegrationTest.java")

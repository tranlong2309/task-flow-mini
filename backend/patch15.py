# -*- coding: utf-8 -*-
import os
import re

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\test\java\com\taskflow\infrastructure\web\controller"

def patch(rel_path):
    path = os.path.join(base_dir, rel_path)
    if os.path.exists(path):
        with open(path, 'r', encoding='utf-8') as f:
            content = f.read()
        content = re.sub(r"@MockBean\s*private JwtService jwtService;", "", content)
        content = re.sub(r"@MockBean\s*private SpringDataRoleRepository roleRepository;", "", content)
        content = re.sub(r"@MockBean\s*private SpringDataTeamRepository teamRepository;", "", content)
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Patched {rel_path}")

patch("NotificationIntegrationTest.java")
patch("ReportIntegrationTest.java")
patch("AuthIntegrationTest.java")

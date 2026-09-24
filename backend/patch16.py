# -*- coding: utf-8 -*-
import os

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\test\java\com\taskflow\infrastructure\web\controller"

def clean(file_name):
    path = os.path.join(base_dir, file_name)
    if os.path.exists(path):
        with open(path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        new_lines = []
        for line in lines:
            if "SpringDataRoleRepository roleRepository;" in line: continue
            if "SpringDataTeamRepository teamRepository;" in line: continue
            if "JwtService jwtService;" in line: continue
            if "@MockBean" in line:
                # Need to be careful. The @MockBean might be on its own line before the field.
                pass
            new_lines.append(line)
        
        # Let's do a robust string replacement on the entire file content instead
        content = "".join(lines)
        content = content.replace("@MockBean\n    private JwtService jwtService;", "")
        content = content.replace("@MockBean\n    private SpringDataRoleRepository roleRepository;", "")
        content = content.replace("@MockBean\n    private SpringDataTeamRepository teamRepository;", "")
        content = content.replace("@MockBean\r\n    private JwtService jwtService;", "")
        content = content.replace("@MockBean\r\n    private SpringDataRoleRepository roleRepository;", "")
        content = content.replace("@MockBean\r\n    private SpringDataTeamRepository teamRepository;", "")

        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Cleaned {file_name}")

clean("NotificationIntegrationTest.java")
clean("ReportIntegrationTest.java")
clean("AuthIntegrationTest.java")

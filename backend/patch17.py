# -*- coding: utf-8 -*-
import os

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\test\java\com\taskflow\infrastructure\web\controller"

def clean(file_name):
    path = os.path.join(base_dir, file_name)
    if os.path.exists(path):
        with open(path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        new_lines = []
        skip_next = False
        for line in lines:
            if skip_next:
                skip_next = False
                continue
            if "@MockBean" in line:
                # peek next line to see if it's the bad import
                idx = lines.index(line)
                if idx + 1 < len(lines):
                    next_line = lines[idx+1]
                    if "JwtService jwtService;" in next_line or "SpringDataRoleRepository roleRepository;" in next_line or "SpringDataTeamRepository teamRepository;" in next_line:
                        skip_next = True
                        continue
            new_lines.append(line)

        with open(path, 'w', encoding='utf-8') as f:
            f.writelines(new_lines)
        print(f"Cleaned {file_name}")

clean("NotificationIntegrationTest.java")
clean("ReportIntegrationTest.java")
clean("AuthIntegrationTest.java")

# -*- coding: utf-8 -*-
import os

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\test\java\com\taskflow\infrastructure\web\controller"

def remove_lines(file_name, strings_to_remove):
    path = os.path.join(base_dir, file_name)
    if os.path.exists(path):
        with open(path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        new_lines = []
        skip_next = False
        for i, line in enumerate(lines):
            if skip_next:
                skip_next = False
                continue
            
            should_remove = False
            for s in strings_to_remove:
                if s in line:
                    should_remove = True
                    break
            
            if "@MockBean" in line:
                if i + 1 < len(lines):
                    next_line = lines[i+1]
                    for s in strings_to_remove:
                        if s in next_line:
                            should_remove = True
                            skip_next = True
                            break
            
            if not should_remove:
                new_lines.append(line)

        with open(path, 'w', encoding='utf-8') as f:
            f.writelines(new_lines)
        print(f"Cleaned lines from {file_name}")

strings = [
    "JwtService jwtService;",
    "SpringDataRoleRepository roleRepository;",
    "SpringDataTeamRepository teamRepository;"
]
remove_lines("NotificationIntegrationTest.java", strings)
remove_lines("ReportIntegrationTest.java", strings)
remove_lines("AuthIntegrationTest.java", strings)

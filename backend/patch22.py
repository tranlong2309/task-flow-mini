# -*- coding: utf-8 -*-
import os

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\test\java\com\taskflow"
path = os.path.join(base_dir, r"application\service\WorkloadApplicationServiceTest.java")

with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(".thenReturn(List.of(task1, task2, task3));", ".thenReturn(new PagedResponse<>(List.of(task1, task2, task3), 3, 1, 0, 100000));")

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Patched WorkloadApplicationServiceTest.java again")

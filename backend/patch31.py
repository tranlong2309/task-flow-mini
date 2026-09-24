# -*- coding: utf-8 -*-
import os

path = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\test\java\com\taskflow\infrastructure\web\controller\TaskIntegrationTest.java"

with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('jsonPath("$[0].title")', 'jsonPath("$.content[0].title")')

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Patched TaskIntegrationTest jsonPath")

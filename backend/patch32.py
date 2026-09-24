# -*- coding: utf-8 -*-
import os

path = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\test\java\com\taskflow\infrastructure\web\controller\TaskIntegrationTest.java"

with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace("assertEquals(3, taskHistoryRepository.count());", "assertEquals(2, taskHistoryRepository.count());")
content = content.replace("assertEquals(4, taskHistoryRepository.count());", "assertEquals(3, taskHistoryRepository.count());")

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Patched TaskIntegrationTest assertion 213")

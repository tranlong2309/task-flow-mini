# -*- coding: utf-8 -*-
import os
import re

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"
path = os.path.join(base_dir, r"infrastructure\persistence\repository\TaskRepositoryAdapter.java")

with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace any sequence of @Override annotations with a single @Override
content = re.sub(r"(@Override\s*){2,}", "@Override\n    ", content)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Cleaned up @Override in TaskRepositoryAdapter")

# -*- coding: utf-8 -*-
import os

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"
path = os.path.join(base_dir, r"infrastructure\persistence\repository\UserRepositoryAdapter.java")

with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

if "import org.springframework.transaction.annotation.Transactional;" not in content:
    content = content.replace("import org.springframework.stereotype.Component;", "import org.springframework.stereotype.Component;\nimport org.springframework.transaction.annotation.Transactional;")

if "@Transactional(readOnly = true)" not in content:
    content = content.replace("public Optional<User> findById(Long id) {", "@Transactional(readOnly = true)\n    public Optional<User> findById(Long id) {")
    content = content.replace("public List<User> findByIds(List<Long> ids) {", "@Transactional(readOnly = true)\n    public List<User> findByIds(List<Long> ids) {")

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Patched UserRepositoryAdapter")

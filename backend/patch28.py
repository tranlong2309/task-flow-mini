# -*- coding: utf-8 -*-
import os

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"
path = os.path.join(base_dir, r"infrastructure\persistence\repository\SpringDataUserRepository.java")

with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace("Optional<UserEntity> findByEmail(String email);", "@org.springframework.data.jpa.repository.EntityGraph(attributePaths = {\"roles\", \"teams\"})\n    Optional<UserEntity> findByEmail(String email);")

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Patched SpringDataUserRepository")

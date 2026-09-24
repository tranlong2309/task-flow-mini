# -*- coding: utf-8 -*-
import os

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"
path = os.path.join(base_dir, r"infrastructure\persistence\entity\UserEntity.java")

with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. FetchType.LAZY on roles
content = content.replace("fetch = FetchType.EAGER", "fetch = FetchType.LAZY")

# 2. Implements Persistable<Long>
if "implements Persistable<Long>" not in content:
    content = content.replace("public class UserEntity {", "public class UserEntity implements org.springframework.data.domain.Persistable<Long> {\n\n    @jakarta.persistence.Transient\n    private boolean isNew = true;\n\n    @jakarta.persistence.Version\n    private Long version;\n")
    content = content.replace("public class UserEntity {\r\n", "public class UserEntity implements org.springframework.data.domain.Persistable<Long> {\r\n\r\n    @jakarta.persistence.Transient\r\n    private boolean isNew = true;\r\n\r\n    @jakarta.persistence.Version\r\n    private Long version;\r\n")

    # Add getId() if missing, though it should exist. Add isNew()
    if "public boolean isNew()" not in content:
        method = '''
    @Override
    public boolean isNew() {
        return isNew;
    }

    @jakarta.persistence.PrePersist
    @jakarta.persistence.PostLoad
    void markNotNew() {
        this.isNew = false;
    }
'''
        content = content.replace("}", method + "}")
    
with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Patched UserEntity")

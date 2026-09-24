import os
import re

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"

def patch(rel_path):
    path = os.path.join(base_dir, rel_path)
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    new_methods = '''
    long countByBoardIdAndDeletedAtIsNull(UUID boardId);
    long countByBoardIdAndStatusColumnIdAndDeletedAtIsNull(UUID boardId, Long statusColumnId);
    long countByBoardIdAndIsBlockedTrueAndDeletedAtIsNull(UUID boardId);
    long countByBoardIdAndDueDateBeforeAndCompletedAtIsNullAndDeletedAtIsNull(UUID boardId, Instant date);
'''
    content = content.replace("}", new_methods + "}")
    
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Patched {rel_path}")

patch(r"infrastructure\persistence\repository\SpringDataTaskRepository.java")

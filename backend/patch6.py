# -*- coding: utf-8 -*-
import os
import re

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"

def patch(rel_path):
    path = os.path.join(base_dir, rel_path)
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    if "TaskRepositoryPort.java" in rel_path:
        content = content.replace("long countByBoardId(UUID boardId);", "long countByBoardId(UUID boardId);\n    long countByBoardIdAndCompletedAtIsNotNull(UUID boardId);")
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Patched port in {rel_path}")

    if "SpringDataTaskRepository.java" in rel_path:
        content = content.replace("long countByBoardIdAndDeletedAtIsNull(UUID boardId);", "long countByBoardIdAndDeletedAtIsNull(UUID boardId);\n    long countByBoardIdAndCompletedAtIsNotNullAndDeletedAtIsNull(UUID boardId);")
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Patched repo in {rel_path}")
        
    if "TaskRepositoryAdapter.java" in rel_path:
        content = content.replace("public long countByBoardId(UUID boardId) {\n        return springDataTaskRepository.countByBoardIdAndDeletedAtIsNull(boardId);\n    }", "public long countByBoardId(UUID boardId) {\n        return springDataTaskRepository.countByBoardIdAndDeletedAtIsNull(boardId);\n    }\n\n    @Override\n    public long countByBoardIdAndCompletedAtIsNotNull(UUID boardId) {\n        return springDataTaskRepository.countByBoardIdAndCompletedAtIsNotNullAndDeletedAtIsNull(boardId);\n    }")
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Patched adapter in {rel_path}")

    # ReportApplicationService getBoardSummary
    if "ReportApplicationService.java" in rel_path:
        content, n = re.subn(
            r"List<Task> tasks = taskRepositoryPort\.searchTasks\(boardId, null, null, null, null, null\);.*?return new BoardReportSummary\(boardId, totalTasks, done, inProgress, blocked, overdue\);",
            r'''
        long totalTasks = taskRepositoryPort.countByBoardId(boardId);
        long done = taskRepositoryPort.countByBoardIdAndCompletedAtIsNotNull(boardId);
        long inProgress = totalTasks - done;
        long blocked = taskRepositoryPort.countByBoardIdAndIsBlockedTrue(boardId);
        long overdue = taskRepositoryPort.countByBoardIdAndDueDateBeforeAndCompletedAtIsNull(boardId, Instant.now());
        
        return new BoardReportSummary(boardId, totalTasks, done, inProgress, blocked, overdue);
''',
            content, count=1, flags=re.MULTILINE | re.DOTALL
        )
        print(f"Patched getBoardSummary in {rel_path} ({n})")
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)

patch(r"domain\repository\TaskRepositoryPort.java")
patch(r"infrastructure\persistence\repository\SpringDataTaskRepository.java")
patch(r"infrastructure\persistence\repository\TaskRepositoryAdapter.java")
patch(r"application\service\ReportApplicationService.java")

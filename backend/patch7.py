# -*- coding: utf-8 -*-
import os

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"
path = os.path.join(base_dir, r"application\service\ReportApplicationService.java")

with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

start_idx = content.find("public BoardReportSummary getBoardSummary(UUID boardId, Long userId) {")
if start_idx != -1:
    end_idx = content.find("return new BoardReportSummary(boardId, totalTasks, done, inProgress, blocked, overdue);", start_idx)
    end_idx += len("return new BoardReportSummary(boardId, totalTasks, done, inProgress, blocked, overdue);")
    
    # next brace
    brace_idx = content.find("}", end_idx)
    
    new_method = '''public BoardReportSummary getBoardSummary(UUID boardId, Long userId) {
        long totalTasks = taskRepositoryPort.countByBoardId(boardId);
        long done = taskRepositoryPort.countByBoardIdAndCompletedAtIsNotNull(boardId);
        long inProgress = totalTasks - done;
        long blocked = taskRepositoryPort.countByBoardIdAndIsBlockedTrue(boardId);
        long overdue = taskRepositoryPort.countByBoardIdAndDueDateBeforeAndCompletedAtIsNull(boardId, Instant.now());
        
        return new BoardReportSummary(boardId, totalTasks, done, inProgress, blocked, overdue);
    }'''

    content = content[:start_idx] + new_method + content[brace_idx+1:]
    
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Patched getBoardSummary")
else:
    print("Could not find getBoardSummary")

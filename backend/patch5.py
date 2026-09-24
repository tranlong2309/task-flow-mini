# -*- coding: utf-8 -*-
import os
import re

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"

def patch(rel_path):
    path = os.path.join(base_dir, rel_path)
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    # WorkloadApplicationService
    if "WorkloadApplicationService.java" in rel_path:
        content, n = re.subn(
            r"List<Task> tasks = taskRepositoryPort\.searchTasks\(boardId, null, null, null, null, null\);",
            r"List<Task> tasks = taskRepositoryPort.searchTasks(boardId, null, null, null, null, null, 0, 100000).content();",
            content, count=1
        )
        print(f"Patched {rel_path} ({n})")

    # ReportApplicationService getBoardSummary
    if "ReportApplicationService.java" in rel_path:
        content, n = re.subn(
            r"List<Task> tasks = taskRepositoryPort\.searchTasks\(boardId, null, null, null, null, null\);(.*?long overdue = 0;.*?for\s*\(Task t : tasks\)\s*\{(.*?)\})",
            r'''
        long totalTasks = taskRepositoryPort.countByBoardId(boardId);
        long done = 0;
        long inProgress = 0;
        
        List<com.taskflow.domain.model.BoardColumn> columns = boardColumnRepositoryPort.findByBoardId(boardId);
        for (com.taskflow.domain.model.BoardColumn col : columns) {
            String name = col.getName().toLowerCase();
            if (name.contains("done") || name.contains("hoàn thành")) {
                done += taskRepositoryPort.countByBoardIdAndStatusColumnId(boardId, col.getId());
            } else if (name.contains("in progress") || name.contains("đang làm")) {
                inProgress += taskRepositoryPort.countByBoardIdAndStatusColumnId(boardId, col.getId());
            }
        }
        
        long blocked = taskRepositoryPort.countByBoardIdAndIsBlockedTrue(boardId);
        long overdue = taskRepositoryPort.countByBoardIdAndDueDateBeforeAndCompletedAtIsNull(boardId, java.time.Instant.now());
''',
            content, count=1, flags=re.MULTILINE | re.DOTALL
        )
        print(f"Patched getBoardSummary in {rel_path} ({n})")
        
        # ReportApplicationService getTeamReport
        content, n2 = re.subn(
            r"List<Task> allTasks = new ArrayList<>\(\);\s*for\s*\(Board board : boards\)\s*\{\s*// Note: date range filter.*?\s*List<Task> boardTasks = taskRepositoryPort\.searchTasks\(board\.getId\(\), null, assigneeId, null, null, null\);\s*allTasks\.addAll\(boardTasks\);\s*\}\s*// Apply date range filter in memory\s*Instant fromInstant = from != null \? Instant\.parse\(from \+ \"T00:00:00Z\"\) : null;\s*Instant toInstant = to != null \? Instant\.parse\(to \+ \"T23:59:59Z\"\) : null;\s*if\s*\(fromInstant != null \|\| toInstant != null\)\s*\{\s*allTasks = allTasks\.stream\(\)\.filter\(t ->\s*\{(.*?)\}\)\.collect\(Collectors\.toList\(\)\);\s*\}",
            r'''
        java.util.List<UUID> boardIds = boards.stream().map(Board::getId).collect(Collectors.toList());
        Instant fromInstant = from != null ? Instant.parse(from + "T00:00:00Z") : null;
        Instant toInstant = to != null ? Instant.parse(to + "T23:59:59Z") : null;
        List<Task> allTasks = boardIds.isEmpty() ? new ArrayList<>() : taskRepositoryPort.searchTasksByBoardIds(boardIds, assigneeId, fromInstant, toInstant);
''',
            content, count=1, flags=re.MULTILINE | re.DOTALL
        )
        print(f"Patched getTeamReport in {rel_path} ({n2})")

    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

patch(r"application\service\WorkloadApplicationService.java")
patch(r"application\service\ReportApplicationService.java")

# -*- coding: utf-8 -*-
import os

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"

def patch(rel_path, replacements):
    path = os.path.join(base_dir, rel_path)
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    for old, new in replacements:
        content = content.replace(old, new)
        
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Patched {rel_path}")

# 1. TaskRepositoryPort (add import Instant)
patch(r"domain\repository\TaskRepositoryPort.java", [
    ("import java.util.UUID;", "import java.util.UUID;\nimport java.time.Instant;")
])

# 2. SpringDataTaskRepository (add import Instant)
patch(r"infrastructure\persistence\repository\SpringDataTaskRepository.java", [
    ("import java.util.UUID;", "import java.util.UUID;\nimport java.time.Instant;")
])

# 3. TaskApplicationService (PagedResponse)
patch(r"application\service\TaskApplicationService.java", [
    ("package com.taskflow.application.service;", "package com.taskflow.application.service;\n\nimport com.taskflow.infrastructure.web.dto.PagedResponse;"),
    ("public List<Task> searchTasks(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly, Long requesterId) {", 
     "public PagedResponse<Task> searchTasks(UUID boardId, Long statusColumnId, Long assigneeId, Priority priority, String search, Boolean overdueOnly, Long requesterId, int page, int size) {"),
    ("return taskRepositoryPort.searchTasks(boardId, statusColumnId, assigneeId, priority, search, overdueOnly);",
     "return taskRepositoryPort.searchTasks(boardId, statusColumnId, assigneeId, priority, search, overdueOnly, page, size);")
])

# 4. ReportApplicationService (getTeamReport and getBoardSummary)
new_board_summary = '''
        long totalTasks = taskRepositoryPort.countByBoardId(boardId);
        long done = taskRepositoryPort.countByBoardIdAndCompletedAtIsNotNull(boardId);
        long inProgress = totalTasks - done;
        long blocked = taskRepositoryPort.countByBoardIdAndIsBlockedTrue(boardId);
        long overdue = taskRepositoryPort.countByBoardIdAndDueDateBeforeAndCompletedAtIsNull(boardId, Instant.now());
'''
patch(r"application\service\ReportApplicationService.java", [
    # getBoardSummary
    (
        "List<Task> tasks = taskRepositoryPort.searchTasks(boardId, null, null, null, null, null);\n\n        Map<Long, String> columnNames = boardColumnRepositoryPort.findByBoardId(boardId).stream()\n                .collect(Collectors.toMap(BoardColumn::getId, BoardColumn::getName));\n\n        long totalTasks = tasks.size();\n        long done = 0;\n        long inProgress = 0;\n        long blocked = 0;\n        long overdue = 0;\n\n        Instant now = Instant.now();\n\n        for (Task task : tasks) {\n            boolean isDone = task.getCompletedAt() != null;\n            if (isDone) {\n                done++;\n            } else {\n                inProgress++;\n            }\n\n            if (Boolean.TRUE.equals(task.getIsBlocked())) {\n                blocked++;\n            }\n\n            if (!isDone && task.getDueDate() != null && task.getDueDate().isBefore(now)) {\n                overdue++;\n            }\n        }",
        new_board_summary
    ),
    # getTeamReport
    (
        "List<Task> allTasks = new ArrayList<>();\n        for (Board board : boards) {\n            // Note: date range filter (from/to) not strictly pushed to DB here for simplicity, but in a real app we'd use Specifications\n            List<Task> boardTasks = taskRepositoryPort.searchTasks(board.getId(), null, assigneeId, null, null, null);\n            allTasks.addAll(boardTasks);\n        }\n\n        // Apply date range filter in memory\n        Instant fromInstant = from != null ? Instant.parse(from + \"T00:00:00Z\") : null;\n        Instant toInstant = to != null ? Instant.parse(to + \"T23:59:59Z\") : null;\n\n        if (fromInstant != null || toInstant != null) {\n            allTasks = allTasks.stream().filter(t -> {\n                Instant date = t.getCreatedAt();\n                if (fromInstant != null && date.isBefore(fromInstant)) return false;\n                if (toInstant != null && date.isAfter(toInstant)) return false;\n                return true;\n            }).collect(Collectors.toList());\n        }",
        '''java.util.List<UUID> boardIds = boards.stream().map(Board::getId).collect(Collectors.toList());
        Instant fromInstant = from != null ? Instant.parse(from + "T00:00:00Z") : null;
        Instant toInstant = to != null ? Instant.parse(to + "T23:59:59Z") : null;
        List<Task> allTasks = boardIds.isEmpty() ? new java.util.ArrayList<>() : taskRepositoryPort.searchTasksByBoardIds(boardIds, assigneeId, fromInstant, toInstant);'''
    )
])

# 5. TaskController (PagedResponse handling, exact replacement to avoid wiping out constructor/dependencies)
patch(r"infrastructure\web\controller\TaskController.java", [
    ("public ResponseEntity<?> searchTasks(@PathVariable UUID boardId,\n                                           @RequestParam(required = false) Long statusColumnId,\n                                           @RequestParam(required = false) Long assigneeId,\n                                           @RequestParam(required = false) Priority priority,\n                                           @RequestParam(required = false) String search,\n                                           @RequestParam(required = false) Boolean overdueOnly,\n                                           @AuthenticationPrincipal CustomUserDetails userDetails) {\n        List<Task> tasks = searchTasksUseCase.searchTasks(boardId, statusColumnId, assigneeId, priority, search, overdueOnly, userDetails.getId());\n        return ResponseEntity.ok(tasks);\n    }",
     '''public ResponseEntity<?> searchTasks(@PathVariable UUID boardId,
                                           @RequestParam(required = false) Long statusColumnId,
                                           @RequestParam(required = false) Long assigneeId,
                                           @RequestParam(required = false) Priority priority,
                                           @RequestParam(required = false) String search,
                                           @RequestParam(required = false) Boolean overdueOnly,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (size > 100) size = 100;
        com.taskflow.infrastructure.web.dto.PagedResponse<Task> paged = searchTasksUseCase.searchTasks(boardId, statusColumnId, assigneeId, priority, search, overdueOnly, userDetails.getId(), page, size);
        return ResponseEntity.ok(paged);
    }'''),
    
    ("public ResponseEntity<?> searchTasksAdvanced(@PathVariable UUID boardId,\n                                                   @RequestParam(required = false) String q,\n                                                   @RequestParam(required = false) String status,\n                                                   @RequestParam(required = false) Long assigneeId,\n                                                   @RequestParam(required = false) Priority priority,\n                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {",
     '''public ResponseEntity<?> searchTasksAdvanced(@PathVariable UUID boardId,
                                                   @RequestParam(required = false) String q,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) Long assigneeId,
                                                   @RequestParam(required = false) Priority priority,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {'''),
                                                   
    ("List<Task> tasks = searchTasksUseCase.searchTasks(boardId, statusColumnId, assigneeId, priority, q, null, userDetails.getId());\n            \n            List<com.taskflow.domain.model.TaskSearchResult> items = tasks.stream().map(t -> \n                new com.taskflow.domain.model.TaskSearchResult(\n                    t.getId(), \n                    t.getTitle(), \n                    colMap.getOrDefault(t.getStatusColumnId(), \"UNKNOWN\"), \n                    t.getAssigneeId(), \n                    t.getPriority(), \n                    t.getDueDate()\n                )\n            ).collect(java.util.stream.Collectors.toList());\n\n            return ResponseEntity.ok(Map.of(\"items\", items));",
     '''if (size > 100) size = 100;
            com.taskflow.infrastructure.web.dto.PagedResponse<Task> paged = searchTasksUseCase.searchTasks(boardId, statusColumnId, assigneeId, priority, q, null, userDetails.getId(), page, size);
            
            List<com.taskflow.domain.model.TaskSearchResult> items = paged.content().stream().map(t -> 
                new com.taskflow.domain.model.TaskSearchResult(
                    t.getId(), 
                    t.getTitle(), 
                    colMap.getOrDefault(t.getStatusColumnId(), "UNKNOWN"), 
                    t.getAssigneeId(), 
                    t.getPriority(), 
                    t.getDueDate()
                )
            ).collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "items", items,
                "totalElements", paged.totalElements(),
                "totalPages", paged.totalPages(),
                "page", paged.page(),
                "size", paged.size()
            ));''')
])


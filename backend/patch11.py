# -*- coding: utf-8 -*-
import os
import re

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"
path = os.path.join(base_dir, r"infrastructure\web\controller\TaskController.java")

with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix searchTasks
content = re.sub(
    r"public ResponseEntity<\?> searchTasks\(@PathVariable UUID boardId,\s*@RequestParam\(required = false\) Long statusColumnId,\s*@RequestParam\(required = false\) Long assigneeId,\s*@RequestParam\(required = false\) Priority priority,\s*@RequestParam\(required = false\) String search,\s*@RequestParam\(required = false\) Boolean overdueOnly,\s*@AuthenticationPrincipal CustomUserDetails userDetails\) \{\s*List<Task> tasks = searchTasksUseCase\.searchTasks\(boardId, statusColumnId, assigneeId, priority, search, overdueOnly, userDetails\.getId\(\)\);\s*return ResponseEntity\.ok\(tasks\);\s*\}",
    '''public ResponseEntity<?> searchTasks(@PathVariable UUID boardId,
                                           @org.springframework.web.bind.annotation.RequestParam(required = false) Long statusColumnId,
                                           @org.springframework.web.bind.annotation.RequestParam(required = false) Long assigneeId,
                                           @org.springframework.web.bind.annotation.RequestParam(required = false) Priority priority,
                                           @org.springframework.web.bind.annotation.RequestParam(required = false) String search,
                                           @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean overdueOnly,
                                           @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
                                           @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (size > 100) size = 100;
        com.taskflow.infrastructure.web.dto.PagedResponse<Task> paged = searchTasksUseCase.searchTasks(boardId, statusColumnId, assigneeId, priority, search, overdueOnly, userDetails.getId(), page, size);
        return ResponseEntity.ok(paged);
    }''',
    content, count=1, flags=re.MULTILINE
)

# Fix searchTasksAdvanced
content = re.sub(
    r"public ResponseEntity<\?> searchTasksAdvanced\(@PathVariable UUID boardId,\s*@RequestParam\(required = false\) String q,\s*@RequestParam\(required = false\) String status,\s*@RequestParam\(required = false\) Long assigneeId,\s*@RequestParam\(required = false\) Priority priority,\s*@AuthenticationPrincipal CustomUserDetails userDetails\) \{(.*?)List<Task> tasks = searchTasksUseCase\.searchTasks\(boardId, statusColumnId, assigneeId, priority, q, null, userDetails\.getId\(\)\);\s*List<com\.taskflow\.domain\.model\.TaskSearchResult> items = tasks\.stream\(\)\.map\(t ->\s*new com\.taskflow\.domain\.model\.TaskSearchResult\(\s*t\.getId\(\),\s*t\.getTitle\(\),\s*colMap\.getOrDefault\(t\.getStatusColumnId\(\), \"UNKNOWN\"\),\s*t\.getAssigneeId\(\),\s*t\.getPriority\(\),\s*t\.getDueDate\(\)\s*\)\s*\)\.collect\(java\.util\.stream\.Collectors\.toList\(\)\);\s*return ResponseEntity\.ok\(Map\.of\(\"items\", items\)\);\s*\}",
    r'''public ResponseEntity<?> searchTasksAdvanced(@PathVariable UUID boardId,
                                                   @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
                                                   @org.springframework.web.bind.annotation.RequestParam(required = false) String status,
                                                   @org.springframework.web.bind.annotation.RequestParam(required = false) Long assigneeId,
                                                   @org.springframework.web.bind.annotation.RequestParam(required = false) Priority priority,
                                                   @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
                                                   @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {\1
        if (size > 100) size = 100;
        com.taskflow.infrastructure.web.dto.PagedResponse<Task> paged = searchTasksUseCase.searchTasks(boardId, statusColumnId, assigneeId, priority, q, null, userDetails.getId(), page, size);
        
        java.util.List<com.taskflow.domain.model.TaskSearchResult> items = paged.content().stream().map(t -> 
            new com.taskflow.domain.model.TaskSearchResult(
                t.getId(), 
                t.getTitle(), 
                colMap.getOrDefault(t.getStatusColumnId(), "UNKNOWN"), 
                t.getAssigneeId(), 
                t.getPriority(), 
                t.getDueDate()
            )
        ).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(java.util.Map.of(
            "items", items,
            "totalElements", paged.totalElements(),
            "totalPages", paged.totalPages(),
            "page", paged.page(),
            "size", paged.size()
        ));
    }''',
    content, count=1, flags=re.MULTILINE | re.DOTALL
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Patched TaskController")

import os
import re

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"

def patch(rel_path):
    path = os.path.join(base_dir, rel_path)
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    # TaskController searchTasks
    pattern1 = r"public ResponseEntity<\?> searchTasks\(@PathVariable UUID boardId,(.*?)(@AuthenticationPrincipal CustomUserDetails userDetails)\) \{(.*?)List<Task> tasks = searchTasksUseCase\.searchTasks\(boardId, statusColumnId, assigneeId, priority, search,\s*overdueOnly, userDetails\.getId\(\)\);\s*return ResponseEntity\.ok\(tasks\);\s*\}"

    repl1 = r'''public ResponseEntity<?> searchTasks(@PathVariable UUID boardId,\1@org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
                                           @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size,
                                           \2) {
        if (size > 100) size = 100;
\3com.taskflow.infrastructure.web.dto.PagedResponse<Task> paged = searchTasksUseCase.searchTasks(boardId, statusColumnId, assigneeId, priority, search, overdueOnly, userDetails.getId(), page, size);
        return ResponseEntity.ok(paged);
    }'''

    content, n1 = re.subn(pattern1, repl1, content, count=1, flags=re.MULTILINE | re.DOTALL)

    # TaskController searchTasksAdvanced
    pattern2 = r"public ResponseEntity<\?> searchTasksAdvanced\(@PathVariable UUID boardId,(.*?)(@AuthenticationPrincipal CustomUserDetails userDetails)\) \{(.*?)List<Task> tasks = searchTasksUseCase\.searchTasks\(boardId, statusColumnId, assigneeId, priority, q,\s*null, userDetails\.getId\(\)\);\s*List<com\.taskflow\.domain\.model\.TaskSearchResult> items = tasks\.stream\(\)\.map\(t ->\s*new com\.taskflow\.domain\.model\.TaskSearchResult\((.*?)\)\s*\)\.collect\(java\.util\.stream\.Collectors\.toList\(\)\);\s*return ResponseEntity\.ok\(Map\.of\(\"items\", items\)\);\s*\}"

    repl2 = r'''public ResponseEntity<?> searchTasksAdvanced(@PathVariable UUID boardId,\1@org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
                                                   @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size,
                                                   \2) {
        if (size > 100) size = 100;
\3com.taskflow.infrastructure.web.dto.PagedResponse<Task> paged = searchTasksUseCase.searchTasks(boardId, statusColumnId, assigneeId, priority, q, null, userDetails.getId(), page, size);
              
              List<com.taskflow.domain.model.TaskSearchResult> items = paged.content().stream().map(t -> 
                  new com.taskflow.domain.model.TaskSearchResult(\4)
              ).collect(java.util.stream.Collectors.toList());
  
              return ResponseEntity.ok(Map.of(
                  "items", items,
                  "totalElements", paged.totalElements(),
                  "totalPages", paged.totalPages(),
                  "page", paged.page(),
                  "size", paged.size()
              ));
          }'''

    content, n2 = re.subn(pattern2, repl2, content, count=1, flags=re.MULTILINE | re.DOTALL)

    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Patched {rel_path} ({n1}, {n2})")

patch(r"infrastructure\web\controller\TaskController.java")

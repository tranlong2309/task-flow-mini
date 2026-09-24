import os
import re

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"

def update_file(rel_path, pattern, replacement):
    path = os.path.join(base_dir, rel_path)
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    content, count = re.subn(pattern, replacement, content, flags=re.MULTILINE | re.DOTALL)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Updated {rel_path} ({count} changes)")

# TaskController searchTasks
pattern1 = r"public ResponseEntity<\?> searchTasks\(@PathVariable UUID boardId,(.*?)(@AuthenticationPrincipal CustomUserDetails userDetails)\) \{(.*?)List<Task> tasks = searchTasksUseCase\.searchTasks\(boardId, statusColumnId, assigneeId, priority, search,\s*overdueOnly, userDetails\.getId\(\)\);\s*return ResponseEntity\.ok\(tasks\);\s*\}"

repl1 = r'''public ResponseEntity<?> searchTasks(@PathVariable UUID boardId,\1@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size,
                                           \2) {
        if (size > 100) size = 100;
\3com.taskflow.infrastructure.web.dto.PagedResponse<Task> paged = searchTasksUseCase.searchTasks(boardId, statusColumnId, assigneeId, priority, search, overdueOnly, userDetails.getId(), page, size);
        return ResponseEntity.ok(paged);
    }'''

update_file(r"infrastructure\web\controller\TaskController.java", pattern1, repl1)

# TaskController searchTasksAdvanced
pattern2 = r"public ResponseEntity<\?> searchTasksAdvanced\(@PathVariable UUID boardId,(.*?)(@AuthenticationPrincipal CustomUserDetails userDetails)\) \{(.*?)List<Task> tasks = searchTasksUseCase\.searchTasks\(boardId, statusColumnId, assigneeId, priority, q,\s*null, userDetails\.getId\(\)\);\s*List<com\.taskflow\.domain\.model\.TaskSearchResult> items = tasks\.stream\(\)\.map\(t ->\s*new com\.taskflow\.domain\.model\.TaskSearchResult\((.*?)\)\s*\)\.collect\(java\.util\.stream\.Collectors\.toList\(\)\);\s*return ResponseEntity\.ok\(Map\.of\(\"items\", items\)\);\s*\}"

repl2 = r'''public ResponseEntity<?> searchTasksAdvanced(@PathVariable UUID boardId,\1@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
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

update_file(r"infrastructure\web\controller\TaskController.java", pattern2, repl2)

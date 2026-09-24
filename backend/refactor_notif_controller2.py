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

pattern = r"public ResponseEntity<\?> getNotifications\(\s*@RequestParam\(required = false\) Long userId,\s*@AuthenticationPrincipal CustomUserDetails userDetails\) \{(.*?)List<UserNotification> items = getNotificationsUseCase\.getNotifications\(targetUserId\);\s*return ResponseEntity\.ok\(Map\.of\(\"items\", items\)\);\s*\}"

repl = r'''public ResponseEntity<?> getNotifications(
              @RequestParam(required = false) Long userId,
              @RequestParam(defaultValue = "0") int page,
              @RequestParam(defaultValue = "20") int size,
              @AuthenticationPrincipal CustomUserDetails userDetails) {\1
        if (size > 100) size = 100;
        com.taskflow.infrastructure.web.dto.PagedResponse<UserNotification> paged = getNotificationsUseCase.getNotifications(targetUserId, page, size);
        return ResponseEntity.ok(Map.of(
            "items", paged.content(),
            "totalElements", paged.totalElements(),
            "totalPages", paged.totalPages(),
            "page", paged.page(),
            "size", paged.size()
        ));
    }'''

update_file(
    r"infrastructure\web\controller\NotificationController.java",
    pattern,
    repl
)

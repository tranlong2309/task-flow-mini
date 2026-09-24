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

pattern = r"public ResponseEntity<\?> getNotifications\(@AuthenticationPrincipal CustomUserDetails userDetails\) \{\s*List<com\.taskflow\.domain\.model\.UserNotification> notifications = getNotificationsUseCase\.getNotifications\(userDetails\.getId\(\)\);\s*return ResponseEntity\.ok\(notifications\);\s*\}"

repl = r'''public ResponseEntity<?> getNotifications(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        if (size > 100) size = 100;
        com.taskflow.infrastructure.web.dto.PagedResponse<com.taskflow.domain.model.UserNotification> paged = getNotificationsUseCase.getNotifications(userDetails.getId(), page, size);
        return ResponseEntity.ok(paged);
    }'''

update_file(
    r"infrastructure\web\controller\NotificationController.java",
    pattern,
    repl
)

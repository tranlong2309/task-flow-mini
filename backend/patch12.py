# -*- coding: utf-8 -*-
import os

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"
path = os.path.join(base_dir, r"infrastructure\web\controller\TaskController.java")

with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace searchTasksAdvanced signature
content = content.replace('''public ResponseEntity<?> searchTasksAdvanced(@PathVariable UUID boardId,
                                                 @RequestParam(required = false) String q,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(required = false) Long assigneeId,
                                                 @RequestParam(required = false) Priority priority,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {''', 
'''public ResponseEntity<?> searchTasksAdvanced(@PathVariable UUID boardId,
                                                 @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
                                                 @org.springframework.web.bind.annotation.RequestParam(required = false) String status,
                                                 @org.springframework.web.bind.annotation.RequestParam(required = false) Long assigneeId,
                                                 @org.springframework.web.bind.annotation.RequestParam(required = false) Priority priority,
                                                 @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
                                                 @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {''')

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Patched TaskController signature")

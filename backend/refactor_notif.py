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

# NotificationRepositoryPort
update_file(
    r"domain\repository\NotificationRepositoryPort.java",
    r"List<UserNotification> getUserNotifications\(Long userId\);",
    r"import com.taskflow.infrastructure.web.dto.PagedResponse;\n\n    PagedResponse<UserNotification> getUserNotifications(Long userId, int page, int size);"
)

# GetNotificationsUseCase
update_file(
    r"application\port\in\GetNotificationsUseCase.java",
    r"List<UserNotification> getNotifications\(Long userId\);",
    r"import com.taskflow.infrastructure.web.dto.PagedResponse;\n\n    PagedResponse<UserNotification> getNotifications(Long userId, int page, int size);"
)

# NotificationApplicationService
update_file(
    r"application\service\NotificationApplicationService.java",
    r"public List<UserNotification> getNotifications\(Long userId\) \{\s*return notificationRepositoryPort\.getUserNotifications\(userId\);\s*\}",
    r"import com.taskflow.infrastructure.web.dto.PagedResponse;\n\n    @Override\n    public PagedResponse<UserNotification> getNotifications(Long userId, int page, int size) {\n        return notificationRepositoryPort.getUserNotifications(userId, page, size);\n    }"
)

# NotificationRepositoryAdapter
update_file(
    r"infrastructure\persistence\repository\NotificationRepositoryAdapter.java",
    r"public List<UserNotification> getUserNotifications\(Long userId\) \{",
    r"import com.taskflow.infrastructure.web.dto.PagedResponse;\nimport org.springframework.data.domain.Page;\nimport org.springframework.data.domain.PageRequest;\n\n    @Override\n    public PagedResponse<UserNotification> getUserNotifications(Long userId, int page, int size) {"
)
update_file(
    r"infrastructure\persistence\repository\NotificationRepositoryAdapter.java",
    r"return springDataNotificationReceiverRepository\.findByUserIdOrderByNotificationIdDesc\(userId\)\.stream\(\)\s*\.map\(this::toDomain\)\s*\.toList\(\);",
    r"PageRequest pageRequest = PageRequest.of(page, size);\n        Page<com.taskflow.infrastructure.persistence.entity.NotificationReceiverEntity> entityPage = springDataNotificationReceiverRepository.findByUserIdOrderByNotificationIdDesc(userId, pageRequest);\n        java.util.List<UserNotification> notifications = entityPage.getContent().stream().map(this::toDomain).toList();\n        return new PagedResponse<>(notifications, entityPage.getTotalElements(), entityPage.getTotalPages(), page, size);"
)


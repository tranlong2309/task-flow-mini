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

pattern = r"public PagedResponse<UserNotification> getUserNotifications\(Long userId, int page, int size\) \{(.*?)\}\s+private Notification toDomain"

repl = r'''public PagedResponse<UserNotification> getUserNotifications(Long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<NotificationReceiverEntity> entityPage = receiverRepository.findByUserIdOrderByNotificationIdDesc(userId, pageRequest);
        List<NotificationReceiverEntity> receivers = entityPage.getContent();
          
        List<Long> notificationIds = receivers.stream().map(NotificationReceiverEntity::getNotificationId).collect(Collectors.toList());
          
        Map<Long, NotificationEntity> notifications = notificationRepository.findAllById(notificationIds).stream()
                .collect(Collectors.toMap(NotificationEntity::getId, n -> n));
  
        List<UserNotification> result = receivers.stream()
                .filter(r -> notifications.containsKey(r.getNotificationId()))
                .map(r -> {
                    NotificationEntity n = notifications.get(r.getNotificationId());
                    return new UserNotification(r.getId(), r.getUserId(), n.getType(), n.getTitle(), n.getMessage(), r.getIsRead(), n.getCreatedAt());
                })
                .collect(Collectors.toList());
                
        return new PagedResponse<>(result, entityPage.getTotalElements(), entityPage.getTotalPages(), page, size);
    }
    
    private Notification toDomain'''

update_file(
    r"infrastructure\persistence\repository\NotificationRepositoryAdapter.java",
    pattern,
    repl
)

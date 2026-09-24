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

update_file(
    r"infrastructure\persistence\repository\SpringDataNotificationReceiverRepository.java",
    r"List<NotificationReceiverEntity> findByUserIdOrderByNotificationIdDesc\(Long userId\);",
    r"org.springframework.data.domain.Page<NotificationReceiverEntity> findByUserIdOrderByNotificationIdDesc(Long userId, org.springframework.data.domain.Pageable pageable);"
)

pattern = r"List<NotificationReceiverEntity> receivers = \s*receiverRepository\.findByUserIdOrderByNotificationIdDesc\(userId\);(.*?)\.collect\(Collectors\.toList\(\)\);\s*return result;"

repl = r'''PageRequest pageRequest = PageRequest.of(page, size);
        Page<NotificationReceiverEntity> entityPage = receiverRepository.findByUserIdOrderByNotificationIdDesc(userId, pageRequest);
        List<NotificationReceiverEntity> receivers = entityPage.getContent();\1.collect(Collectors.toList());
        return new PagedResponse<>(result, entityPage.getTotalElements(), entityPage.getTotalPages(), page, size);'''

update_file(
    r"infrastructure\persistence\repository\NotificationRepositoryAdapter.java",
    pattern,
    repl
)

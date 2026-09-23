# FE-05: Notification center & reminder banner

## Mô tả
Hiển thị thông báo trong app cho member và manager, bao gồm reminder deadline, task mới và quá hạn.

## UI/UX cần có
- Bell icon with unread count
- Notification list with read state
- Toast khi có thông báo mới
- Mark as read action

## API cần gọi
- `GET /api/v1/notifications?userId=7`
- `PATCH /api/v1/notifications/{notificationId}/read`

## State management
- Notifications query cached
- Unread count derived from list
- Optional polling for local app updates

## Dependencies
- BE-05: notification API

## Acceptance criteria
- User thấy số lượng thông báo chưa đọc.
- Notification được đánh dấu đã đọc khi click.
- Không có duplicate toast cho cùng một event.

## Complexity
Small

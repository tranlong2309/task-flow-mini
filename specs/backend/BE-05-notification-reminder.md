# BE-05: API notification & reminder

## Mô tả
Task này xử lý thông báo nhắc việc, reminder trước deadline và cảnh báo khi task quá hạn. Thuộc Story 7 và Story 8.

## Scope
- Gửi notification khi task mới/giao
- Gửi remind trước deadline và overdue
- Đánh dấu notification đã đọc
- Không spam trong cùng 1 sự kiện

## API contract

### 1) Lấy notification
- Method: `GET`
- Endpoint: `/api/v1/notifications?userId=7`
- Response:
```json
{
  "items": [
    {
      "id": 801,
      "userId": 7,
      "type": "DEADLINE_SOON",
      "title": "Task sắp hết hạn",
      "message": "Task 'Design landing page' hết hạn trong 1 ngày",
      "read": false,
      "createdAt": "2026-09-23T08:45:00Z"
    }
  ]
}
```

### 2) Đánh dấu đọc
- Method: `PATCH`
- Endpoint: `/api/v1/notifications/{notificationId}/read`

### 3) Cron job reminder
- Method: internal scheduler
- Endpoint: `/api/v1/reminders/run` hoặc scheduled job internal

## Data model / DB thay đổi
- Bảng `notifications`
- Bảng `notification_receivers`

```sql
CREATE TABLE notifications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  type VARCHAR(50) NOT NULL,
  title VARCHAR(255) NOT NULL,
  message TEXT NOT NULL,
  related_task_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL,
  expires_at TIMESTAMP NULL
);

CREATE TABLE notification_receivers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  notification_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  is_read BOOLEAN DEFAULT FALSE,
  read_at TIMESTAMP NULL,
  FOREIGN KEY (notification_id) REFERENCES notifications(id)
);
```

## Business rules
- Không gửi duplicate notification cho cùng 1 sự kiện trong vòng 24h.
- Người nhận mặc định: assignee + manager của board.
- Mốc reminder: 1 ngày, 3 ngày, 7 ngày trước deadline; overdue.
- Nếu task đã được cập nhật mới trong thời gian gần deadline, reminder không được gửi tiếp khi không cần thiết.

## Dependencies
- BE-02: task CRUD
- BE-03: status changes
- BE-08: auth & RBAC

## Acceptance criteria
- Manager nhận notification khi task sắp quá hạn.
- Member nhận thông báo khi task mới được giao hoặc deadline thay đổi.
- Notification có thể đánh dấu đã đọc.
- Duplicate notification không phát sinh.

## Complexity
Medium

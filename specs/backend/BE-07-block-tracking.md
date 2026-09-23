# BE-07: API block / reason tracking

## Mô tả
Task này xử lý trạng thái blocked và nguyên nhân chặn. Nó thuộc Story 10.

## Scope
- Set task block
- Unblock task
- Lưu reason và thời điểm block

## API contract

### 1) Block task
- Method: `PATCH`
- Endpoint: `/api/v1/tasks/{taskId}/block`
- Request:
```json
{
  "reason": "Waiting for approved design from client",
  "blockedAt": "2026-09-23T09:19:00Z"
}
```
- Response:
```json
{
  "id": 5001,
  "isBlocked": true,
  "blockedReason": "Waiting for approved design from client",
  "updatedAt": "2026-09-23T09:19:00Z"
}
```

### 2) Unblock task
- Method: `PATCH`
- Endpoint: `/api/v1/tasks/{taskId}/unblock`

## Data model / DB thay đổi
- Thêm cột `is_blocked`, `blocked_reason`, `blocked_at` vào `tasks`

```sql
ALTER TABLE tasks
  ADD COLUMN is_blocked BOOLEAN DEFAULT FALSE,
  ADD COLUMN blocked_reason TEXT NULL,
  ADD COLUMN blocked_at TIMESTAMP NULL;
```

## Business rules
- `blocked_reason` bắt buộc khi set `is_blocked = true`.
- Task đang blocked không được coi là hoàn thành.
- Chỉ assignee hoặc manager/admin được phép block/unblock.
- Nếu `reason` chưa nhập, reject request với `400`.

## Dependencies
- BE-02: task CRUD
- BE-03: status flow

## Acceptance criteria
- Task có thể ghi nhận nguyên nhân block.
- Dashboard hiển thị task blocked rõ ràng.
- Nếu không điền lý do, API reject đúng quy tắc.

## Complexity
Small

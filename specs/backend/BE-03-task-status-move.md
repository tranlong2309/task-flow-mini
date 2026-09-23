# BE-03: API cập nhật trạng thái task & drag/drop

## Mô tả
Task này cho phép chuyển task qua lại giữa các cột và cập nhật trạng thái. Nó thuộc Story 3 và Story 5. Đây là phần cốt lõi của Kanban board.

## Scope
- Update status theo cột
- Move task trong cùng board bằng drag/drop
- Lưu timeline/history cho thay đổi trạng thái

## API contract

### 1) Update status
- Method: `PATCH`
- Endpoint: `/api/v1/tasks/{taskId}/status`
- Request:
```json
{
  "statusColumnId": 3,
  "note": "Waiting for PM review",
  "blockedReason": null
}
```
- Response:
```json
{
  "id": 5001,
  "statusColumnId": 3,
  "status": "REVIEW",
  "position": 5,
  "updatedAt": "2026-09-23T09:00:00Z",
  "note": "Waiting for PM review"
}
```

### 2) Move task
- Method: `PATCH`
- Endpoint: `/api/v1/tasks/{taskId}/move`
- Request:
```json
{
  "sourceColumnId": 1,
  "targetColumnId": 2,
  "sourceIndex": 2,
  "targetIndex": 0
}
```
- Response:
```json
{
  "id": 5001,
  "fromColumnId": 1,
  "toColumnId": 2,
  "fromIndex": 2,
  "toIndex": 0,
  "updatedAt": "2026-09-23T09:00:00Z"
}
```

## Data model / DB thay đổi
- Sử dụng trường `status_column_id` trong `tasks`
- Có thể thêm `position` nếu hệ thống cần sắp xếp trong một cột

### Schema gợi ý
```sql
ALTER TABLE tasks
  ADD COLUMN status_column_id BIGINT NOT NULL,
  ADD COLUMN position INT DEFAULT 0,
  ADD COLUMN completed_at TIMESTAMP NULL,
  ADD COLUMN updated_at TIMESTAMP NOT NULL;
```

## Business rules
- `statusColumnId` phải thuộc board hiện tại của task.
- Không cho move task giữa các board khác nhau.
- Khi chuyển vào `Done`, set `completed_at`.
- Khi chuyển ra khỏi `Done`, clear `completed_at`.
- Thao tác move phải atomic transaction.
- Cập nhật trạng thái phải ghi lịch sử thay đổi.

## Dependencies
- BE-01: board API
- BE-02: task CRUD

## Acceptance criteria
- Task có thể kéo thả từ cột Todo sang In Progress.
- Khi move, vị trí của task trong column được cập nhật đúng.
- Task vẫn giữ assignee, due date, boardId sau khi đổi cột.
- Nếu request column không hợp lệ, trả `400 Bad Request`.

## Complexity
Medium

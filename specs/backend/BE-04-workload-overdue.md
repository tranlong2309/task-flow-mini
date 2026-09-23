# BE-04: API workload & overdue tracking

## Mô tả
Task này trả về dữ liệu tổng quan cho board và assignee. Nó phục vụ dashboard workload, theo dõi task quá hạn và cảnh báo manager.

## Scope
- Tổng quan board
- Số lượng task theo assignee
- Task quá hạn, task blocked
- Workload cá nhân / board

## API contract

### 1) Workload summary
- Method: `GET`
- Endpoint: `/api/v1/boards/{boardId}/workload`
- Response:
```json
{
  "boardId": 101,
  "summary": {
    "total": 28,
    "todo": 8,
    "inProgress": 11,
    "review": 5,
    "done": 4,
    "overdue": 3,
    "blocked": 2
  },
  "byAssignee": [
    {
      "assigneeId": 7,
      "fullName": "Lan",
      "activeTasks": 6,
      "overdueTasks": 1,
      "blockedTasks": 1
    }
  ]
}
```

### 2) Danh sách overdue
- Method: `GET`
- Endpoint: `/api/v1/boards/{boardId}/tasks?overdueOnly=true`

## Data model / DB thay đổi
- Không cần bảng mới.
- Dùng dữ liệu từ `tasks`, `users`, `board_members`.

## Business rules
- `overdue` = task chưa hoàn thành và `due_date < now()`.
- `blocked` = task có `is_blocked = true` hoặc trạng thái cột blocked.
- Manager có quyền xem workload toàn board; member chỉ xem card của mình hoặc summary limited.
- Nếu board không có task, phải trả về zero summary thay vì lỗi.

## Dependencies
- BE-02: task CRUD
- BE-03: status move
- BE-08: auth & RBAC

## Acceptance criteria
- Tổng số task đúng với dữ liệu đang có.
- Tasks quá hạn hiển thị rõ và có thể filter.
- Member không thể xem workload của người khác nếu không có quyền.

## Complexity
Medium

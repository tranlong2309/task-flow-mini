# BE-06: API dashboard/reporting

## Mô tả
Task này xây dựng API tổng quan dashboard và báo cáo tiến độ, phục vụ manager. Nó thuộc Story 9 và Story 10.

## Scope
- Summary board metrics
- Report theo project/team
- Task bị block
- Data export khả dụng ở mức ưu tiên thấp (CSV)

## API contract

### 1) Summary board
- Method: `GET`
- Endpoint: `/api/v1/reports/boards/{boardId}/summary`
- Response:
```json
{
  "boardId": 101,
  "generatedAt": "2026-09-23T09:15:00Z",
  "totalTasks": 28,
  "done": 4,
  "inProgress": 11,
  "blocked": 2,
  "overdue": 3,
  "completionRate": 14.3
}
```

### 2) Team report
- Method: `GET`
- Endpoint: `/api/v1/reports/team/{teamId}?from=2026-09-01&to=2026-09-30`

## Data model / DB thay đổi
- Không cần schema mới ở MVP.
- Query dựa trên `tasks`, `boards`, `users`, `board_members`.

## Business rules
- Summary phải trả về metrics theo phạm vi board hiện tại.
- Chỉ manager/admin được phép xem báo cáo toàn team/board.
- Nếu không có dữ liệu, trả về zero values thay vì lỗi.
- Completion rate = `done / total * 100`.

## Dependencies
- BE-04: workload
- BE-05: notification

## Acceptance criteria
- Dashboard hiển thị đúng số task chưa hoàn thành, hoàn thành, quá hạn, blocked.
- Manager có thể filter theo assignee hoặc date range.
- Task bị block hiển thị trong sơ đồ hoặc danh sách báo cáo.

## Complexity
Medium

# FE-03: Task detail drawer / sidebar

## Mô tả
Drawer hiển thị chi tiết task, trạng thái hiện tại, note và blocker. Cho phép sửa nhanh từ task detail.

## UI/UX cần có
- Task detail panel sliding from right
- Section overview / blocker / activity
- Update status nhanh
- Empty state nếu task không tồn tại

## API cần gọi
- `GET /api/v1/tasks/{taskId}`
- `PATCH /api/v1/tasks/{taskId}/status`
- `PATCH /api/v1/tasks/{taskId}/block`
- `PATCH /api/v1/tasks/{taskId}/unblock`

## State management
- Query detail task
- Local UI state for drawer open/close
- Selected task stored in Zustand

## Dependencies
- BE-02: task CRUD
- BE-03: task status
- BE-07: block tracking

## Acceptance criteria
- Drawer mở đúng task tương ứng.
- Thay đổi status cập nhật ngay trên UI và server.
- Task blocked có note visible rõ ràng.

## Complexity
Medium

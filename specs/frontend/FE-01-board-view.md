# FE-01: Board view và cột Kanban

## Mô tả
Màn hình board chính hiển thị task theo từng cột, cho phép kéo thả giữa các trạng thái và filter nhanh theo assignee/priority.

## UI/UX cần có
- Loading skeleton khi lấy board data
- Empty state nếu board chưa có task
- Error state nếu fetch fail
- Drag and drop giữa cột
- Highlight drop zone
- Filter bar theo assignee, status, priority

## API cần gọi
- `GET /api/v1/boards/{boardId}`
- `GET /api/v1/boards/{boardId}/tasks`
- `PATCH /api/v1/tasks/{taskId}/move`

## State management
- Board dữ liệu cached với TanStack Query
- Filters, selected board, sort state in Zustand
- Optimistic update khi drag/drop

## Dependencies
- BE-01: board API
- BE-02: task API
- BE-03: status move

## Acceptance criteria
- Board render đúng cột và task theo trạng thái.
- Drag/drop thành công và cập nhật server.
- Nếu API lỗi, UI rollback trạng thái hợp lý.

## Complexity
Large

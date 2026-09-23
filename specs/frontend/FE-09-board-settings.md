# FE-09: Board settings & column configuration

## Mô tả
Màn hình cài đặt board cho phép manager tạo, đổi tên, sắp xếp và cấu hình cột trạng thái quản lý task.

## UI/UX cần có
- Settings modal hoặc panel
- Add/edit column form
- Reorder with drag/drop or up/down buttons
- Warning khi xóa cột đang có task

## API cần gọi
- `POST /api/v1/boards`
- `PUT /api/v1/boards/{boardId}`
- `GET /api/v1/boards/{boardId}`

## State management
- Board settings form local state
- Invalidate board query after save
- Save pending status UI

## Dependencies
- BE-01: board API

## Acceptance criteria
- Manager tạo board mới thành công.
- Thay đổi tên cột lưu đúng và render lại UI.
- Nếu cột đang chứa task, hệ thống cảnh báo và từ chối xóa.

## Complexity
Medium

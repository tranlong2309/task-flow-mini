# FE-08: Search & filter nhanh

## Mô tả
Cho phép tìm nhanh task và lọc theo trạng thái, assignee, deadline hoặc urgent items.

## UI/UX cần có
- Search bar trong header
- Quick filter chips
- Debounced search
- Empty result state

## API cần gọi
- `GET /api/v1/boards/{boardId}/tasks/search?q=...`
- `GET /api/v1/boards/{boardId}/tasks`

## State management
- Search query state
- Debounce local state
- Cache result per board

## Dependencies
- BE-09: search & filter API

## Acceptance criteria
- Search từ title trả đúng task.
- Filter by assignee/status cập nhật board ngay.
- Không có lỗi khi query rỗng.

## Complexity
Small

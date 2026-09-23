# FE-06: Dashboard báo cáo tiến độ

## Mô tả
Trang báo cáo tổng quan cho manager, hiển thị trạng thái task, progress và blocker trong board/team.

## UI/UX cần có
- Summary cards
- Chart hoặc list metrics
- Dropdown filter by team, assignee, date range
- Export CSV button

## API cần gọi
- `GET /api/v1/reports/boards/{boardId}/summary`
- `GET /api/v1/reports/team/{teamId}?from=...&to=...`

## State management
- Report query cache
- Filters in Zustand
- Debounce date range update

## Dependencies
- BE-06: dashboard reporting

## Acceptance criteria
- Manager xem đúng số liệu theo board/team.
- Nếu không có dữ liệu, hiển thị zero state rõ ràng.
- Export CSV trả về dữ liệu có đúng format.

## Complexity
Medium

# FE-04: Workload overview & overdue list

## Mô tả
Dashboard manager xem tổng quan workload, assignee đang quá tải và task quá hạn.

## UI/UX cần có
- Summary cards
- List theo assignee
- Filter by team/assignee
- Highlight overdue and blocked items
- Empty state

## API cần gọi
- `GET /api/v1/boards/{boardId}/workload`
- `GET /api/v1/boards/{boardId}/tasks?overdueOnly=true`

## State management
- Dashboard summary cache by boardId
- Optional polling every 60s
- Filter state in Zustand

## Dependencies
- BE-04: workload API

## Acceptance criteria
- Manager nhìn thấy workload theo assignee rõ ràng.
- Task quá hạn được đánh dấu highlight.
- Nếu không có task, hiển thị empty state.

## Complexity
Medium

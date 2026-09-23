# FE-02: Modal tạo/sửa task

## Mô tả
Form tạo và chỉnh sửa task, cho phép nhập title, description, assignee, priority, deadline, status.

## UI/UX cần có
- Form validation inline
- Save/Cancel button
- Error banner khi API reject
- Disabled state khi đang submit

## API cần gọi
- `POST /api/v1/tasks`
- `PUT /api/v1/tasks/{taskId}`
- `GET /api/v1/users/me`
- `GET /api/v1/boards/{boardId}/tasks`

## State management
- Local form state
- Mutation state via TanStack Query
- Invalidate board query after save

## Dependencies
- BE-02: task CRUD
- BE-08: auth & RBAC

## Acceptance criteria
- User tạo task mới với assignee/deadline hợp lệ.
- Missing required field hiển thị validation rõ.
- Task mới render trên board sau khi save.

## Complexity
Medium

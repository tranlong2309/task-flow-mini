# FE-07: Auth, session & role gating

## Mô tả
Quản lý đăng nhập, session và quyền hiển thị các phần của app theo role. Đây là phần base cho tất cả giao diện.

## UI/UX cần có
- Login page
- Protected routes
- Access denied page
- Board list theo team

## API cần gọi
- `GET /api/v1/users/me`
- `GET /api/v1/boards/{boardId}/permissions`

## State management
- Auth state in Zustand
- Persist session token or cookie
- Route guards based on role

## Dependencies
- BE-08: auth & RBAC

## Acceptance criteria
- User không login không truy cập được app.
- Manager và member thấy các route phù hợp với quyền.
- Nếu không có quyền, hiển thị access denied rõ ràng.

## Complexity
Medium

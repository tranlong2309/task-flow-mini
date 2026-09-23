# BE-08: API auth & RBAC

## Mô tả
Task này cung cấp xác thực người dùng và kiểm soát quyền theo role (member / manager / admin), phục vụ cho toàn bộ hệ thống.

## Scope
- Login/session
- Lấy thông tin user hiện tại
- Phân quyền theo board và team
- Kiểm tra quyền action

## API contract

### 1) User profile
- Method: `GET`
- Endpoint: `/api/v1/users/me`
- Response:
```json
{
  "id": 7,
  "name": "Lan",
  "email": "lan@agency.vn",
  "role": "MEMBER",
  "teams": [12, 13]
}
```

### 2) Permissions board
- Method: `GET`
- Endpoint: `/api/v1/boards/{boardId}/permissions`
- Response:
```json
{
  "canCreateTask": true,
  "canEditTask": true,
  "canDeleteBoard": false,
  "canViewReport": true
}
```

## Data model / DB thay đổi
- Bảng `users`
- Bảng `roles`
- Bảng `user_roles`
- Bảng `team_members`
- Bảng `board_members`

## Business rules
- `ADMIN` có toàn quyền trên tất cả board.
- `MANAGER` có quyền quản lý board, task, report.
- `MEMBER` chỉ được tạo/chỉnh sửa task mình hoặc task được gán.
- User cần thuộc board để truy cập dữ liệu board.
- Nếu không có quyền, trả `403 Forbidden`.

## Dependencies
- none

## Acceptance criteria
- User login hợp lệ được truy cập board info.
- User không có quyền chỉnh sửa board khác trả `403`.
- Permission được kiểm tra chính xác ở service layer.

## Complexity
Medium

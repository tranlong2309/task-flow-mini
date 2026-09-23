# BE-01: API tạo/cập nhật board

## Mô tả
Task này xây dựng API cho bộ phận quản lý board. Nó thuộc Story 1: "Quản lý board và task" và là nền tảng cho toàn bộ hệ thống Kanban.

## Scope
- Tạo board mới
- Chỉnh sửa thông tin board
- Cấu hình danh sách cột trạng thái
- Xem chi tiết board

## API contract

### 1) Tạo board
- Method: `POST`
- Endpoint: `/api/v1/boards`
- Request body:
```json
{
  "name": "Marketing Campaign 2026",
  "description": "Board theo dõi các task marketing",
  "teamId": 12,
  "statusColumns": [
    { "name": "Todo", "order": 1 },
    { "name": "In Progress", "order": 2 },
    { "name": "Review", "order": 3 },
    { "name": "Done", "order": 4 }
  ]
}
```
- Response: `201 Created`
```json
{
  "id": 101,
  "name": "Marketing Campaign 2026",
  "description": "Board theo dõi các task marketing",
  "teamId": 12,
  "statusColumns": [
    { "id": 1, "name": "Todo", "order": 1 },
    { "id": 2, "name": "In Progress", "order": 2 },
    { "id": 3, "name": "Review", "order": 3 },
    { "id": 4, "name": "Done", "order": 4 }
  ],
  "createdAt": "2026-09-23T08:00:00Z"
}
```

### 2) Cập nhật board
- Method: `PUT`
- Endpoint: `/api/v1/boards/{boardId}`
- Request body: tương tự payload create, cho phép thay đổi tên, description, cột.

### 3) Lấy chi tiết board
- Method: `GET`
- Endpoint: `/api/v1/boards/{boardId}`
- Response:
```json
{
  "id": 101,
  "name": "Marketing Campaign 2026",
  "description": "Board theo dõi các task marketing",
  "teamId": 12,
  "statusColumns": [
    { "id": 1, "name": "Todo", "order": 1 },
    { "id": 2, "name": "In Progress", "order": 2 },
    { "id": 3, "name": "Review", "order": 3 },
    { "id": 4, "name": "Done", "order": 4 }
  ],
  "createdBy": 5,
  "updatedAt": "2026-09-23T08:30:00Z"
}
```

## Data model / DB thay đổi
- Bảng `boards`
- Bảng `board_members`
- Bảng `board_columns`

### Ví dụ schema
```sql
CREATE TABLE boards (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  team_id BIGINT NOT NULL,
  created_by BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  deleted_at TIMESTAMP NULL
);

CREATE TABLE board_columns (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  board_id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  position INT NOT NULL,
  is_default BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL,
  FOREIGN KEY (board_id) REFERENCES boards(id)
);
```

## Business rules
- Chỉ `MANAGER` hoặc `ADMIN` mới tạo/sửa board.
- `name` bắt buộc, tối đa 100 ký tự.
- Mỗi board cần tối thiểu 1 cột; ở MVP tối đa 10 cột.
- Không cho xóa column đang chứa task.
- `Done` phải tồn tại nếu có nhiều hơn 1 cột.
- Board membership phải hợp lệ với team.

## Dependencies
- BE-08: Auth & RBAC
- BE-10: Database migration

## Acceptance criteria
- Manager tạo board thành công với danh sách cột mặc định.
- Nếu thiếu name hoặc không có quyền, API trả `400` hoặc `403`.
- Board hiển thị đúng trạng thái và thứ tự cột.

## Complexity
Medium

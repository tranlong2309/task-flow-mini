# BE-02: API quản lý task CRUD

## Mô tả
Task này xây dựng API cho việc tạo, xem, cập nhật và xoá task trong board. Nó thuộc Story 2 và Story 4, hỗ trợ giao việc, cập nhật deadline và priority.

## Scope
- Tạo task mới
- Cập nhật task
- Xem chi tiết task
- Lấy danh sách task theo board
- Xoá mềm task

## API contract

### 1) Tạo task
- Method: `POST`
- Endpoint: `/api/v1/tasks`
- Request:
```json
{
  "boardId": 101,
  "title": "Design landing page",
  "description": "Create hero section and CTA",
  "assigneeId": 7,
  "priority": "HIGH",
  "dueDate": "2026-09-30T17:00:00Z",
  "statusColumnId": 1,
  "ownerId": 5
}
```
- Response: `201 Created`
```json
{
  "id": 5001,
  "boardId": 101,
  "title": "Design landing page",
  "status": "TODO",
  "assigneeId": 7,
  "priority": "HIGH",
  "dueDate": "2026-09-30T17:00:00Z",
  "createdBy": 5,
  "createdAt": "2026-09-23T08:30:00Z"
}
```

### 2) Cập nhật task
- Method: `PUT`
- Endpoint: `/api/v1/tasks/{taskId}`
- Request:
```json
{
  "title": "Design landing page v2",
  "description": "Update hero section and CTA copy",
  "assigneeId": 8,
  "priority": "MEDIUM",
  "dueDate": "2026-10-02T17:00:00Z"
}
```
- Response: `200 OK`

### 3) Lấy chi tiết task
- Method: `GET`
- Endpoint: `/api/v1/tasks/{taskId}`

### 4) Lấy danh sách task theo board
- Method: `GET`
- Endpoint: `/api/v1/boards/{boardId}/tasks`
- Query params: `status`, `assigneeId`, `priority`, `search`, `overdueOnly`

## Data model / DB thay đổi
- Bảng `tasks`
- Bảng `task_history`

### Schema gợi ý
```sql
CREATE TABLE tasks (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  board_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  status_column_id BIGINT NOT NULL,
  assignee_id BIGINT,
  priority ENUM('LOW','MEDIUM','HIGH') NOT NULL,
  due_date TIMESTAMP NULL,
  created_by BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  deleted_at TIMESTAMP NULL,
  FOREIGN KEY (board_id) REFERENCES boards(id)
);

CREATE TABLE task_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  field_name VARCHAR(50) NOT NULL,
  old_value TEXT,
  new_value TEXT,
  changed_by BIGINT NOT NULL,
  changed_at TIMESTAMP NOT NULL,
  FOREIGN KEY (task_id) REFERENCES tasks(id)
);
```

## Business rules
- `title` không được null/empty.
- `assigneeId` phải thuộc board hiện tại.
- `dueDate` bắt buộc ở MVP khi tạo task active.
- Chỉ `MANAGER`, `ADMIN`, hoặc assignee có quyền update task tương ứng.
- Soft delete: task không bị mất hẳn khỏi DB để audit/history còn nguyên.
- Mỗi update status hoặc owner phải ghi vào `task_history`.

## Dependencies
- BE-01: board API
- BE-08: auth & RBAC

## Acceptance criteria
- Task mới được tạo thành công với đầy đủ thông tin bắt buộc.
- Nếu thiếu owner hoặc due date, trả về validation error rõ ràng.
- Cập nhật assignee, deadline, priority phải lưu đúng và show trên board.
- Board filter theo assignee hoặc priority hoạt động đúng.

## Complexity
Large

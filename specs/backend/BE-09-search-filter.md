# BE-09: API search & filter tasks

## Mô tả
Task này tạo API tìm kiếm và lọc task theo tiêu đề, assignee, status, ưu tiên và deadline. Nó phục vụ board view và quản lý workload.

## Scope
- Search theo title/description
- Filter theo assignee, status, priority
- Tìm task quá hạn / blocked

## API contract

### Search & filter
- Method: `GET`
- Endpoint: `/api/v1/boards/{boardId}/tasks/search?q=design&status=TODO&assigneeId=7`
- Response:
```json
{
  "items": [
    {
      "id": 5001,
      "title": "Design landing page",
      "status": "TODO",
      "assigneeId": 7,
      "priority": "HIGH",
      "dueDate": "2026-09-30T17:00:00Z"
    }
  ]
}
```

## Data model / DB thay đổi
- Không cần bảng mới.
- Cần index trên `title`, `assignee_id`, `status_column_id`, `due_date`.

## Business rules
- Search chỉ trả về task trong board đang được xem.
- Query tối đa 100 ký tự.
- Nếu không có từ khóa, có thể trả về tất cả task theo filter.

## Dependencies
- BE-02: task CRUD

## Acceptance criteria
- Search theo title trả đúng task.
- Filter assignee + status hoạt động đúng.
- Người dùng không thể search task ngoài board hiện tại.

## Complexity
Small

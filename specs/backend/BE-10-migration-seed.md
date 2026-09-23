# BE-10: Database migration & seed data

## Mô tả
Task này tạo migration ban đầu cho schema database và dữ liệu demo để phát triển, test và QA. Nó là nền cho toàn bộ hệ thống.

## Scope
- Migration Flyway cho schema chính
- Seed users, team, board, task mẫu
- Dữ liệu mẫu cho demo và test

## API contract
- Không có public API.

## Data model / DB thay đổi
- Flyway V1-Vn scripts

### Example seed data
```sql
INSERT INTO users (id, name, email, role) VALUES
(1, 'Admin', 'admin@taskflow.vn', 'ADMIN'),
(2, 'Lan', 'lan@taskflow.vn', 'MEMBER');

INSERT INTO boards (id, name, description, team_id, created_by) VALUES
(1, 'Marketing Board', 'Demo board', 1, 1);

INSERT INTO tasks (id, board_id, title, description, status_column_id, assignee_id, priority, due_date) VALUES
(1, 1, 'Design hero banner', 'Create banner hero', 1, 2, 'HIGH', NOW());
```

## Business rules
- Không dùng hard delete cho task và board để retain audit data.
- Seed data phải rõ ràng cho QA và demo.
- Migration phải chạy idempotent trong môi trường CI.

## Dependencies
- all backend domain tasks

## Acceptance criteria
- Migrate database thành công trên môi trường local/dev.
- Seed data có đủ task, board, user, permission để demo nhanh.
- Không có conflict giữa migration mới và dữ liệu cũ.

## Complexity
Medium

BE-08 — Auth & RBAC (làm trước tiên, không phụ thuộc gì)

Bạn đang làm việc trên repo task-flow-mini (Spring Boot 3.x, Java 21, Hexagonal Architecture: domain/application/infrastructure/adapters, MariaDB 10.11+, Flyway, base path /api/v1).

Nhiệm vụ: Implement BE-08 — API auth & RBAC.

Scope:
- Login/session
- Lấy thông tin user hiện tại
- Phân quyền theo board và team (role: MEMBER / MANAGER / ADMIN)
- Kiểm tra quyền action ở service layer

API contract cần implement:
1) GET /api/v1/users/me
   Response 200:
   { "id": 7, "name": "Lan", "email": "lan@agency.vn", "role": "MEMBER", "teams": [12, 13] }

2) GET /api/v1/boards/{boardId}/permissions
   Response 200:
   { "canCreateTask": true, "canEditTask": true, "canDeleteBoard": false, "canViewReport": true }

Data model (Flyway migration):
- users, roles, user_roles, team_members, board_members

Business rules:
- ADMIN có toàn quyền trên tất cả board.
- MANAGER có quyền quản lý board, task, report.
- MEMBER chỉ được tạo/chỉnh sửa task của mình hoặc task được gán.
- User phải thuộc board mới được truy cập dữ liệu board đó.
- Không có quyền → trả 403 Forbidden.

Acceptance criteria:
- User login hợp lệ truy cập được board info.
- User không có quyền chỉnh sửa board khác → 403.
- Permission check nằm ở service layer (không chỉ ở controller/filter), viết unit test cho permission logic.

Yêu cầu triển khai:
- Tuân theo Hexagonal Architecture của repo: domain model + port trong domain/application, JWT hoặc session auth trong infrastructure/adapters.
- Viết Flyway migration cho các bảng liên quan.
- Viết unit test cho use case và permission checker; integration test cho 2 endpoint trên.
- Không sửa các module khác ngoài phạm vi auth/RBAC.

BE-10 — Database migration & seed data (nền tảng, nên làm sớm song song BE-08)

Bạn đang làm việc trên repo task-flow-mini (Spring Boot 3.x, Java 21, Hexagonal Architecture, MariaDB 10.11+, Flyway).

Nhiệm vụ: Implement BE-10 — Database migration & seed data.

Scope:
- Viết Flyway migration (V1...Vn) cho toàn bộ schema chính của hệ thống (users, roles, teams, boards, board_columns, board_members, tasks, task_history, notifications, notification_receivers, và các cột bổ sung của block-tracking).
- Seed dữ liệu mẫu: users, team, board, task cho môi trường dev/demo/QA.

Ví dụ seed data:
INSERT INTO users (id, name, email, role) VALUES
(1, 'Admin', 'admin@taskflow.vn', 'ADMIN'),
(2, 'Lan', 'lan@taskflow.vn', 'MEMBER');

INSERT INTO boards (id, name, description, team_id, created_by) VALUES
(1, 'Marketing Board', 'Demo board', 1, 1);

INSERT INTO tasks (id, board_id, title, description, status_column_id, assignee_id, priority, due_date) VALUES
(1, 1, 'Design hero banner', 'Create banner hero', 1, 2, 'HIGH', NOW());

Business rules:
- Không dùng hard delete cho task và board (dùng soft delete với deleted_at) để giữ audit trail.
- Seed data phải đủ rõ ràng để QA/demo dùng ngay (ít nhất 1 board đủ 4 cột, vài task ở nhiều trạng thái/priority khác nhau, 1 admin, vài member).
- Migration phải chạy idempotent trong môi trường CI (không lỗi khi chạy lại từ đầu trên DB sạch).

Acceptance criteria:
- Migrate thành công trên local/dev.
- Seed data đủ để demo nhanh toàn bộ flow (board, task, user, permission).
- Không có conflict giữa migration mới và dữ liệu cũ khi các spec khác (BE-01 → BE-09) thêm bảng/cột sau này — thiết kế migration numbering rõ ràng, để dành chỗ cho các migration tiếp theo.

Yêu cầu triển khai:
- Đặt tất cả migration trong đúng thư mục Flyway convention (src/main/resources/db/migration).
- Ghi rõ comment mô tả mục đích từng file migration.

BE-01 — API tạo/cập nhật board (phụ thuộc BE-08, BE-10)

Bạn đang làm việc trên repo task-flow-mini (Spring Boot 3.x, Java 21, Hexagonal Architecture, MariaDB 10.11+, Flyway, base path /api/v1).
Giả định BE-08 (auth & RBAC) và BE-10 (migration nền) đã có sẵn.

Nhiệm vụ: Implement BE-01 — API tạo/cập nhật board.

Scope:
- Tạo board mới
- Chỉnh sửa thông tin board
- Cấu hình danh sách cột trạng thái
- Xem chi tiết board

API contract:
1) POST /api/v1/boards
   Request:
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
   Response 201: board object đầy đủ id, statusColumns (có id), createdAt.

2) PUT /api/v1/boards/{boardId}
   Request tương tự payload create, cho phép đổi name, description, statusColumns.

3) GET /api/v1/boards/{boardId}
   Response: board + statusColumns + createdBy + updatedAt.

Data model (Flyway migration mới, nối tiếp version của BE-10):
- boards(id, name, description, team_id, created_by, created_at, updated_at, deleted_at)
- board_columns(id, board_id, name, position, is_default, created_at)
- board_members (nếu chưa có ở BE-10)

Business rules:
- Chỉ MANAGER hoặc ADMIN mới tạo/sửa board.
- name bắt buộc, tối đa 100 ký tự.
- Mỗi board tối thiểu 1 cột, tối đa 10 cột ở MVP.
- Không cho xóa column đang chứa task.
- Cột "Done" phải tồn tại nếu board có nhiều hơn 1 cột.
- Board membership phải hợp lệ với team.

Dependencies: BE-08 (auth & RBAC), BE-10 (migration).

Acceptance criteria:
- Manager tạo board thành công với danh sách cột mặc định.
- Thiếu name hoặc không có quyền → 400 hoặc 403 tương ứng.
- Board hiển thị đúng trạng thái và thứ tự cột.

Yêu cầu triển khai:
- Domain model Board/BoardColumn trong domain layer, use case trong application layer, REST adapter + JPA repository trong infrastructure/adapters.
- Validate input ở use case, không chỉ ở controller.
- Viết unit test cho business rules (giới hạn cột, cột Done bắt buộc, xóa cột có task) và integration test cho 3 endpoint trên.

BE-02 — API quản lý task CRUD (phụ thuộc BE-01, BE-08)

Bạn đang làm việc trên repo task-flow-mini (Spring Boot 3.x, Java 21, Hexagonal Architecture, MariaDB 10.11+, Flyway, base path /api/v1).
Giả định BE-01 (board API) và BE-08 (auth & RBAC) đã có sẵn.

Nhiệm vụ: Implement BE-02 — API quản lý task CRUD.

Scope:
- Tạo task mới
- Cập nhật task
- Xem chi tiết task
- Lấy danh sách task theo board
- Xoá mềm task

API contract:
1) POST /api/v1/tasks
   Request:
   {
     "boardId": 101, "title": "Design landing page", "description": "Create hero section and CTA",
     "assigneeId": 7, "priority": "HIGH", "dueDate": "2026-09-30T17:00:00Z",
     "statusColumnId": 1, "ownerId": 5
   }
   Response 201: task object đầy đủ id, status, createdBy, createdAt.

2) PUT /api/v1/tasks/{taskId}
   Cho phép cập nhật title, description, assigneeId, priority, dueDate. Response 200.

3) GET /api/v1/tasks/{taskId}

4) GET /api/v1/boards/{boardId}/tasks
   Query params: status, assigneeId, priority, search, overdueOnly

Data model (Flyway migration mới):
- tasks(id, board_id, title, description, status_column_id, assignee_id, priority[LOW/MEDIUM/HIGH], due_date, created_by, created_at, updated_at, deleted_at)
- task_history(id, task_id, field_name, old_value, new_value, changed_by, changed_at)

Business rules:
- title không được null/empty, tối đa 255 ký tự.
- assigneeId phải thuộc board hiện tại.
- dueDate bắt buộc khi tạo task active ở MVP.
- Chỉ MANAGER, ADMIN, hoặc chính assignee mới có quyền update task tương ứng.
- Soft delete: task không mất hẳn khỏi DB (dùng deleted_at) để giữ audit/history.
- Mỗi lần update status hoặc owner phải ghi vào task_history.

Dependencies: BE-01 (board API), BE-08 (auth & RBAC).

Acceptance criteria:
- Task mới tạo thành công với đầy đủ thông tin bắt buộc.
- Thiếu owner hoặc due date → validation error rõ ràng (400 với message cụ thể field lỗi).
- Cập nhật assignee/deadline/priority lưu đúng và hiển thị đúng trên board.
- Filter theo assignee hoặc priority trên board hoạt động đúng.

Yêu cầu triển khai:
- Ghi task_history mỗi khi field quan trọng thay đổi (dùng domain event hoặc explicit call trong use case, theo kiến trúc hexagonal đã dùng ở các module khác).
- Unit test cho validation rules và quyền update; integration test cho 4 endpoint và cho việc ghi task_history.

BE-03 — API cập nhật trạng thái task & drag/drop (phụ thuộc BE-01, BE-02)

Bạn đang làm việc trên repo task-flow-mini (Spring Boot 3.x, Java 21, Hexagonal Architecture, MariaDB 10.11+, Flyway, base path /api/v1).
Giả định BE-01 và BE-02 đã có sẵn (board, task CRUD).

Nhiệm vụ: Implement BE-03 — API cập nhật trạng thái task & drag/drop.

Scope:
- Update status theo cột
- Move task trong cùng board bằng drag/drop
- Lưu timeline/history cho thay đổi trạng thái

API contract:
1) PATCH /api/v1/tasks/{taskId}/status
   Request: { "statusColumnId": 3, "note": "Waiting for PM review", "blockedReason": null }
   Response: { "id": 5001, "statusColumnId": 3, "status": "REVIEW", "position": 5, "updatedAt": "...", "note": "..." }

2) PATCH /api/v1/tasks/{taskId}/move
   Request: { "sourceColumnId": 1, "targetColumnId": 2, "sourceIndex": 2, "targetIndex": 0 }
   Response: { "id": 5001, "fromColumnId": 1, "toColumnId": 2, "fromIndex": 2, "toIndex": 0, "updatedAt": "..." }

Data model (Flyway migration, ALTER TABLE tasks):
- ADD COLUMN status_column_id BIGINT NOT NULL (nếu chưa có từ BE-02)
- ADD COLUMN position INT DEFAULT 0
- ADD COLUMN completed_at TIMESTAMP NULL
- ADD COLUMN updated_at TIMESTAMP NOT NULL (nếu chưa có)

Business rules:
- statusColumnId phải thuộc board hiện tại của task; nếu không → 400 Bad Request.
- Không cho move task giữa các board khác nhau.
- Khi chuyển vào cột "Done" → set completed_at.
- Khi chuyển ra khỏi "Done" → clear completed_at.
- Thao tác move phải atomic transaction (đảm bảo consistency vị trí các task khác trong cột khi reorder).
- Mọi thay đổi trạng thái phải ghi vào task_history (dùng lại cơ chế từ BE-02).

Dependencies: BE-01 (board API), BE-02 (task CRUD).

Acceptance criteria:
- Task kéo thả được từ cột Todo sang In Progress.
- Sau khi move, vị trí (position) của task trong column được cập nhật đúng, không làm lệch vị trí các task khác.
- Task vẫn giữ nguyên assignee, due date, boardId sau khi đổi cột.
- Request column không hợp lệ → 400 Bad Request.

Yêu cầu triển khai:
- Đảm bảo transaction @Transactional bao trọn thao tác move + reorder position.
- Viết unit test cho: set/clear completed_at khi vào/ra Done, atomic move, reject move khác board.
- Viết integration test cho 2 endpoint trên, gồm case reorder nhiều task trong cùng cột.

BE-07 — API block / reason tracking (phụ thuộc BE-02, BE-03 — làm trước BE-04/06 vì các API đó đọc dữ liệu blocked)

Bạn đang làm việc trên repo task-flow-mini (Spring Boot 3.x, Java 21, Hexagonal Architecture, MariaDB 10.11+, Flyway, base path /api/v1).
Giả định BE-02 (task CRUD) và BE-03 (status flow) đã có sẵn.

Nhiệm vụ: Implement BE-07 — API block / reason tracking.

Scope:
- Set task block
- Unblock task
- Lưu reason và thời điểm block

API contract:
1) PATCH /api/v1/tasks/{taskId}/block
   Request: { "reason": "Waiting for approved design from client", "blockedAt": "2026-09-23T09:19:00Z" }
   Response: { "id": 5001, "isBlocked": true, "blockedReason": "Waiting for approved design from client", "updatedAt": "..." }

2) PATCH /api/v1/tasks/{taskId}/unblock

Data model (ALTER TABLE tasks):
- ADD COLUMN is_blocked BOOLEAN DEFAULT FALSE
- ADD COLUMN blocked_reason TEXT NULL
- ADD COLUMN blocked_at TIMESTAMP NULL

Business rules:
- blocked_reason bắt buộc khi set is_blocked = true; thiếu → 400.
- Task đang blocked không được coi là hoàn thành (không cho tự động set completed_at kể cả nếu đang ở cột Done — cần phối hợp logic với BE-03).
- Chỉ assignee hoặc manager/admin được phép block/unblock.

Dependencies: BE-02 (task CRUD), BE-03 (status flow).

Acceptance criteria:
- Task ghi nhận được nguyên nhân block.
- Dashboard (dữ liệu phục vụ BE-04/BE-06) hiển thị rõ task blocked.
- Không điền lý do khi block → API reject đúng quy tắc (400).

Yêu cầu triển khai:
- Cân nhắc rule chồng chéo với BE-03 (task blocked mà đang ở cột Done thì completed_at nên xử lý ra sao) — viết rõ trong code comment và cover bằng test.
- Unit test cho validate reason bắt buộc, quyền block/unblock; integration test cho 2 endpoint.

BE-04 — API workload & overdue tracking (phụ thuộc BE-02, BE-03, BE-08)

Bạn đang làm việc trên repo task-flow-mini (Spring Boot 3.x, Java 21, Hexagonal Architecture, MariaDB 10.11+, Flyway, base path /api/v1).
Giả định BE-02, BE-03, BE-07 (block tracking), BE-08 đã có sẵn.

Nhiệm vụ: Implement BE-04 — API workload & overdue tracking.

Scope:
- Tổng quan board
- Số lượng task theo assignee
- Task quá hạn, task blocked
- Workload cá nhân / board

API contract:
1) GET /api/v1/boards/{boardId}/workload
   Response:
   {
     "boardId": 101,
     "summary": { "total": 28, "todo": 8, "inProgress": 11, "review": 5, "done": 4, "overdue": 3, "blocked": 2 },
     "byAssignee": [
       { "assigneeId": 7, "fullName": "Lan", "activeTasks": 6, "overdueTasks": 1, "blockedTasks": 1 }
     ]
   }

2) GET /api/v1/boards/{boardId}/tasks?overdueOnly=true (dùng lại endpoint từ BE-02, đảm bảo filter hoạt động đúng)

Data model: không cần bảng mới, query trên tasks, users, board_members.

Business rules:
- overdue = task chưa hoàn thành và due_date < now().
- blocked = task có is_blocked = true (dùng field từ BE-07).
- Manager có quyền xem workload toàn board; member chỉ xem card của mình hoặc summary giới hạn (dựa vào permission từ BE-08).
- Board không có task → trả về summary toàn zero, không lỗi.

Dependencies: BE-02 (task CRUD), BE-03 (status move), BE-08 (auth & RBAC), BE-07 (block tracking cho field is_blocked).

Acceptance criteria:
- Tổng số task khớp đúng dữ liệu thực tế.
- Task quá hạn hiển thị rõ và filter được.
- Member không xem được workload người khác nếu không có quyền.

Yêu cầu triển khai:
- Query hiệu quả (tránh N+1), cân nhắc dùng aggregate query ở repository layer.
- Áp dụng permission check từ BE-08 trước khi trả dữ liệu theo assignee.
- Unit test cho tính toán overdue/blocked; integration test cho endpoint workload với board rỗng và board có dữ liệu.

BE-05 — API notification & reminder (phụ thuộc BE-02, BE-03, BE-08)

Bạn đang làm việc trên repo task-flow-mini (Spring Boot 3.x, Java 21, Hexagonal Architecture, MariaDB 10.11+, Flyway, base path /api/v1).
Giả định BE-02, BE-03, BE-08 đã có sẵn.

Nhiệm vụ: Implement BE-05 — API notification & reminder.

Scope:
- Gửi notification khi task mới/giao
- Gửi remind trước deadline và khi overdue
- Đánh dấu notification đã đọc
- Không spam trong cùng 1 sự kiện

API contract:
1) GET /api/v1/notifications?userId=7
   Response: { "items": [ { "id": 801, "userId": 7, "type": "DEADLINE_SOON", "title": "...", "message": "...", "read": false, "createdAt": "..." } ] }

2) PATCH /api/v1/notifications/{notificationId}/read

3) Cron job reminder: nội bộ scheduler (Spring @Scheduled), endpoint /api/v1/reminders/run có thể để internal/admin-only nếu cần trigger thủ công.

Data model (Flyway migration):
- notifications(id, type, title, message, related_task_id, created_at, expires_at)
- notification_receivers(id, notification_id, user_id, is_read, read_at)

Business rules:
- Không gửi duplicate notification cho cùng 1 sự kiện trong vòng 24h (cần cơ chế dedupe, ví dụ theo (type, related_task_id, ngày)).
- Người nhận mặc định: assignee + manager của board.
- Mốc reminder: 1 ngày, 3 ngày, 7 ngày trước deadline; và khi overdue.
- Nếu task vừa được cập nhật gần deadline, không gửi reminder không cần thiết (tránh trùng lặp logic).

Dependencies: BE-02 (task CRUD), BE-03 (status changes), BE-08 (auth & RBAC).

Acceptance criteria:
- Manager nhận notification khi task sắp quá hạn.
- Member nhận thông báo khi task mới giao hoặc deadline thay đổi.
- Notification đánh dấu đã đọc được.
- Không phát sinh duplicate notification.

Yêu cầu triển khai:
- Dùng Spring @Scheduled cho cron reminder job, chạy theo interval hợp lý (ví dụ mỗi giờ), idempotent.
- Cân nhắc domain event khi task được tạo/giao/đổi deadline để trigger notification ngay (không chỉ dựa vào cron).
- Unit test cho dedupe logic và tính mốc reminder (1/3/7 ngày, overdue); integration test cho 2 endpoint REST.

BE-06 — API dashboard/reporting (phụ thuộc BE-04, BE-05)

Bạn đang làm việc trên repo task-flow-mini (Spring Boot 3.x, Java 21, Hexagonal Architecture, MariaDB 10.11+, Flyway, base path /api/v1).
Giả định BE-04 (workload) và BE-05 (notification) đã có sẵn.

Nhiệm vụ: Implement BE-06 — API dashboard/reporting.

Scope:
- Summary board metrics
- Report theo project/team
- Task bị block
- Data export CSV (ưu tiên thấp, có thể để cuối)

API contract:
1) GET /api/v1/reports/boards/{boardId}/summary
   Response:
   {
     "boardId": 101, "generatedAt": "2026-09-23T09:15:00Z",
     "totalTasks": 28, "done": 4, "inProgress": 11, "blocked": 2, "overdue": 3,
     "completionRate": 14.3
   }

2) GET /api/v1/reports/team/{teamId}?from=2026-09-01&to=2026-09-30

Data model: không cần schema mới, query trên tasks, boards, users, board_members.

Business rules:
- Summary phải trả về metrics đúng phạm vi board hiện tại.
- Chỉ manager/admin được xem báo cáo toàn team/board (check qua BE-08).
- Không có dữ liệu → trả về zero values, không lỗi.
- completionRate = done / total * 100 (làm tròn 1 chữ số thập phân, xử lý chia cho 0 khi total = 0 → trả 0).

Dependencies: BE-04 (workload), BE-05 (notification, dùng chung permission/data pattern).

Acceptance criteria:
- Dashboard hiển thị đúng số task chưa hoàn thành, hoàn thành, quá hạn, blocked.
- Manager filter được theo assignee hoặc date range (endpoint team report).
- Task bị block hiển thị trong danh sách/report.

Yêu cầu triển khai:
- Tái sử dụng logic tính overdue/blocked đã có ở BE-04 thay vì viết lại (extract shared query/service nếu hợp lý).
- CSV export (nếu làm) đặt ở endpoint riêng, không bắt buộc cho MVP — có thể để comment TODO nếu không nằm trong phạm vi ưu tiên hiện tại.
- Unit test cho completionRate (bao gồm case total = 0); integration test cho 2 endpoint.

BE-09 — API search & filter tasks (phụ thuộc BE-02, có thể làm song song với BE-04/05/06)

Bạn đang làm việc trên repo task-flow-mini (Spring Boot 3.x, Java 21, Hexagonal Architecture, MariaDB 10.11+, Flyway, base path /api/v1).
Giả định BE-02 (task CRUD) đã có sẵn.

Nhiệm vụ: Implement BE-09 — API search & filter tasks.

Scope:
- Search theo title/description
- Filter theo assignee, status, priority
- Tìm task quá hạn / blocked

API contract:
GET /api/v1/boards/{boardId}/tasks/search?q=design&status=TODO&assigneeId=7
Response:
{
  "items": [
    { "id": 5001, "title": "Design landing page", "status": "TODO", "assigneeId": 7, "priority": "HIGH", "dueDate": "2026-09-30T17:00:00Z" }
  ]
}

Data model:
- Không cần bảng mới.
- Thêm index trên title, assignee_id, status_column_id, due_date (viết migration Flyway riêng cho các index này).

Business rules:
- Search chỉ trả về task trong board đang xem (không leak task board khác).
- Query string tối đa 100 ký tự (validate và trả 400 nếu vượt).
- Không có từ khóa → có thể trả về tất cả task theo filter còn lại.

Dependencies: BE-02 (task CRUD).

Acceptance criteria:
- Search theo title trả đúng task.
- Filter assignee + status hoạt động đúng cùng lúc (kết hợp AND).
- Người dùng không thể search task ngoài board hiện tại (kể cả qua boardId khác trong query).

Yêu cầu triển khai:
- Viết migration Flyway thêm index cần thiết.
- Đảm bảo query search dùng parameterized query, tránh SQL injection dù dùng JPA/Query builder.
- Unit test cho validate query length; integration test cho các tổ hợp filter.
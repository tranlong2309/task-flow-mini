prompt specs BE-08 : Bạn đang làm việc trên repo task-flow-mini (Spring Boot 3.x, Java 21, Hexagonal Architecture: domain/application/infrastructure/adapters, MariaDB 10.11+, Flyway, base path /api/v1).

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
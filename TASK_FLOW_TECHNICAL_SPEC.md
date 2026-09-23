# Task Flow - Technical Specification

## 1. Assumptions & stack

- Backend: Spring Boot 3.x + Java 21, Hexagonal Architecture (domain/application/infrastructure/adapters), MariaDB 10.11+, Flyway for migrations.
- Frontend: React + TypeScript, Vite, TanStack Query, Zustand, Tailwind CSS, shadcn/ui.
- Auth: role-based access control (member / manager / admin) via JWT or session-based auth. For MVP, app supports team-scoped access with board-level permissions.
- Base API prefix: `/api/v1`
- Timezone: store as UTC in DB, display in user local timezone on frontend.
- Notification: MVP supports in-app notifications + email reminders; no real-time websocket required in Phase 1 unless we choose simple polling.

---

## 2. Backend Tasks

### BE-01: API tạo/cập nhật board
- Mô tả: Dùng cho Story 1 “Quản lý board và task”. Cho phép Manager tạo board, cập nhật thông tin board, cấu hình cột trạng thái và gán team / project.
- API contract:
  - `POST /api/v1/boards`
    - Request:
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
    - Response 201:
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
  - `PUT /api/v1/boards/{boardId}`
    - Request: tương tự nhưng cho phép update `name`, `description`, `statusColumns`.
  - `GET /api/v1/boards/{boardId}`
    - Response: board + columns + owner info.
- Data model / DB thay đổi:
  - Bảng: `boards`, `board_members`, `board_columns`
  - Migration:
    - `boards(id, name, description, team_id, created_by, created_at, updated_at, deleted_at)`
    - `board_members(board_id, user_id, role, joined_at)`
    - `board_columns(id, board_id, name, position, is_default, created_at)`
- Business rules:
  - Chỉ `MANAGER`/`ADMIN` mới tạo board hoặc sửa board.
  - Board name không trống, tối đa 100 ký tự.
  - Mỗi board tối thiểu 1 cột, tối đa 10 cột ở MVP.
  - Không cho phép xóa cột nếu đang chứa task.
  - `Done` phải tồn tại nếu board có ít nhất 1 cột.
- Dependencies: BE-09 (RBAC), BE-10 (team membership) nếu có.
- Ước lượng độ phức tạp: Medium

### BE-02: API quản lý task CRUD
- Mô tả: Dùng cho Story 2, 4, 5. Cho phép tạo, sửa, xoá task (soft delete), gán assignee, priority, deadline, mô tả, tags nếu cần.
- API contract:
  - `POST /api/v1/tasks`
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
    - Response 201:
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
  - `PUT /api/v1/tasks/{taskId}`
  - `GET /api/v1/tasks/{taskId}`
  - `GET /api/v1/boards/{boardId}/tasks`
    - Query: `status`, `assigneeId`, `priority`, `search`, `overdueOnly`
- Data model / DB thay đổi:
  - Bảng: `tasks`, `task_assignees` (nếu cho phép multi-assignee), `task_history`.
  - Migration:
    - `tasks(id, board_id, title, description, status_column_id, assignee_id, priority, due_date, created_by, created_at, updated_at, deleted_at)`
    - `task_history(id, task_id, field_name, old_value, new_value, changed_by, changed_at)`
- Business rules:
  - Title không được trống; tối đa 255 ký tự.
  - Assignee phải thuộc board.
  - Deadline bắt buộc đối với task active (trừ trường hợp task có `isNoDeadline` nếu cần). Với MVP, deadline bắt buộc ở task creation.
  - Chỉ owner / manager / admin có quyền update task.
  - Khi đổi status, cập nhật `updated_at` và ghi `task_history`.
- Dependencies: BE-01, BE-09.
- Ước lượng độ phức tạp: Large

### BE-03: API cập nhật trạng thái task & drag/drop
- Mô tả: Dùng cho Story 3, 5. Cho phép chuyển task giữa các cột, tính toán `position` theo thứ tự và lưu lịch sử thay đổi trạng thái.
- API contract:
  - `PATCH /api/v1/tasks/{taskId}/status`
    - Request:
      ```json
      {
        "statusColumnId": 3,
        "note": "Waiting for PM review",
        "blockedReason": null
      }
      ```
    - Response:
      ```json
      {
        "id": 5001,
        "statusColumnId": 3,
        "status": "REVIEW",
        "position": 5,
        "updatedAt": "2026-09-23T09:00:00Z",
        "note": "Waiting for PM review"
      }
      ```
  - `PATCH /api/v1/tasks/{taskId}/move`
    - Request:
      ```json
      {
        "sourceColumnId": 1,
        "targetColumnId": 2,
        "sourceIndex": 2,
        "targetIndex": 0
      }
      ```
- Data model / DB thay đổi:
  - Bảng: `task_positions` hoặc `tasks.position` trong cùng bảng `tasks`.
  - Thêm trường `status`/`status_column_id` nếu không dùng enum riêng.
- Business rules:
  - Trạng thái phải là cột hợp lệ thuộc board.
  - Không cho phép task ở board khác chuyển sang cột của board khác.
  - Move phải atomic transaction; cập nhật vị trí đồng thời cần lock row hoặc use `ORDER BY` + transaction.
  - Nếu task chuyển sang `Done`, hệ thống ghi `completedAt`.
- Dependencies: BE-01, BE-02.
- Ước lượng độ phức tạp: Medium

### BE-04: API workload & overdue tracking
- Mô tả: Dùng cho Story 6, 9. Cung cấp số liệu workload theo assignee, task quá hạn, task blocked và tổng quan board.
- API contract:
  - `GET /api/v1/boards/{boardId}/workload`
    - Response:
      ```json
      {
        "boardId": 101,
        "summary": {
          "total": 28,
          "todo": 8,
          "inProgress": 11,
          "review": 5,
          "done": 4,
          "overdue": 3,
          "blocked": 2
        },
        "byAssignee": [
          {
            "assigneeId": 7,
            "fullName": "Lan",
            "activeTasks": 6,
            "overdueTasks": 1,
            "blockedTasks": 1
          }
        ]
      }
      ```
  - `GET /api/v1/boards/{boardId}/tasks?overdueOnly=true`
- Data model / DB thay đổi:
  - Không cần bảng mới. Dùng `tasks` + `board_members`.
- Business rules:
  - `overdue` = task chưa hoàn thành và `due_date < now()`.
  - `blocked` = task trạng thái `BLOCKED` hoặc `status_column` logic gắn `is_blocked=true`.
  - Chỉ manager/admin xem workload của toàn board; member chỉ xem workload cá nhân hoặc board khi được authorize.
- Dependencies: BE-02, BE-03.
- Ước lượng độ phức tạp: Medium

### BE-05: API notification & reminder
- Mô tả: Dùng cho Story 7, 8. Tạo notification cho assignee/manager khi task mới, deadline sắp tới hoặc quá hạn, và cho phép đánh dấu đã đọc.
- API contract:
  - `GET /api/v1/notifications?userId=7`
    - Response:
      ```json
      {
        "items": [
          {
            "id": 801,
            "userId": 7,
            "type": "DEADLINE_SOON",
            "title": "Task sắp hết hạn",
            "message": "Task 'Design landing page' hết hạn trong 1 ngày",
            "read": false,
            "createdAt": "2026-09-23T08:45:00Z"
          }
        ]
      }
      ```
  - `PATCH /api/v1/notifications/{notificationId}/read`
  - `POST /api/v1/reminders/run` (internal cron) - không phải public API.
- Data model / DB thay đổi:
  - Bảng: `notifications`, `notification_receivers`, `reminder_jobs` nếu cần schedule.
  - Migration:
    - `notifications(id, type, title, message, related_task_id, created_at, expires_at)`
    - `notification_receivers(notification_id, user_id, is_read, read_at)`
- Business rules:
  - Không lặp duplicate notification cho cùng một sự kiện trong 24h.
  - Người nhận mặc định là assignee + manager khi task ở cùng board.
  - Thời gian nhắc: `1 day before`, `3 days before`, `7 days before`, overdue.
  - Notification only send if user is active in team/board.
- Dependencies: BE-02, BE-03, BE-09.
- Ước lượng độ phức tạp: Medium

### BE-06: API dashboard/reporting
- Mô tả: Dùng cho Story 9, 10. Tạo báo cáo tổng quan board và filter theo project/team/assignee.
- API contract:
  - `GET /api/v1/reports/boards/{boardId}/summary`
    - Response:
      ```json
      {
        "boardId": 101,
        "generatedAt": "2026-09-23T09:15:00Z",
        "totalTasks": 28,
        "done": 4,
        "inProgress": 11,
        "blocked": 2,
        "overdue": 3,
        "completionRate": 14.3
      }
      ```
  - `GET /api/v1/reports/team/{teamId}?from=2026-09-01&to=2026-09-30`
- Data model / DB thay đổi:
  - Không cần bảng mới cho MVP; tạo view/query dựa trên `tasks`, `boards`, `users`, `board_members`.
- Business rules:
  - Summary phải tính theo board scope hiện tại.
  - Chỉ allow người có quyền xem báo cáo theo team/board.
  - Nếu no data, trả về zero metrics thay vì lỗi.
- Dependencies: BE-04, BE-05.
- Ước lượng độ phức tạp: Medium

### BE-07: API block / reason tracking
- Mô tả: Dùng cho Story 10. Cho phép task chuyển sang trạng thái block, nhập blocker reason và xem lịch sử.
- API contract:
  - `PATCH /api/v1/tasks/{taskId}/block`
    - Request:
      ```json
      {
        "reason": "Waiting for approved design from client",
        "blockedAt": "2026-09-23T09:19:00Z"
      }
      ```
    - Response:
      ```json
      {
        "id": 5001,
        "isBlocked": true,
        "blockedReason": "Waiting for approved design from client",
        "updatedAt": "2026-09-23T09:19:00Z"
      }
      ```
  - `PATCH /api/v1/tasks/{taskId}/unblock`
- Data model / DB thay đổi:
  - Thêm `is_blocked`, `blocked_reason`, `blocked_at` trong `tasks`.
- Business rules:
  - Khi `is_blocked = true`, task không được coi là `done` và có warning trên dashboard.
  - Bắt buộc phải có `blocked_reason` khi block; nếu người dùng cố tình xóa, hệ thống reject.
  - Chỉ assignee hoặc manager có quyền set/unset blocked.
- Dependencies: BE-02, BE-03.
- Ước lượng độ phức tạp: Small

### BE-08: API auth & RBAC
- Mô tả: Xử lý quyền theo role và board membership. Đây là base required cho toàn bộ feature.
- API contract:
  - `GET /api/v1/users/me`
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
  - `GET /api/v1/boards/{boardId}/permissions`
    - Response:
      ```json
      {
        "canCreateTask": true,
        "canEditTask": true,
        "canDeleteBoard": false,
        "canViewReport": true
      }
      ```
- Data model / DB thay đổi:
  - Bảng: `users`, `roles`, `user_roles`, `team_members`, `board_members`.
- Business rules:
  - `ADMIN` full access.
  - `MANAGER` can update board, task, notifications, reports.
  - `MEMBER` can create/update own tasks and view assigned tasks.
  - Board membership is required to access task details.
- Dependencies: none.
- Ước lượng độ phức tạp: Medium

### BE-09: API search & filter tasks
- Mô tả: Hỗ trợ tìm nhanh task theo title / assignee / status / due date, phục vụ board và dashboard.
- API contract:
  - `GET /api/v1/boards/{boardId}/tasks/search?q=design&status=TODO&assigneeId=7`
    - Response: list tasks phù hợp.
- Data model / DB thay đổi:
  - Không cần schema mới. Dùng `LIKE` query hoặc full-text index nếu data lớn. Có thể cần index trên `title`, `assignee_id`, `status_column_id`, `due_date`.
- Business rules:
  - Search từ title và description, nhưng không trả về task của board khác.
  - `q` tối đa 100 ký tự.
- Dependencies: BE-02.
- Ước lượng độ phức tạp: Small

### BE-10: Database migration & seed data
- Mô tả: Tạo migration ban đầu cho board, task, notification, user/team cấu trúc. Seed dữ liệu mẫu để demo.
- API contract:
  - Không có API public.
- Data model / DB thay đổi:
  - Flyway scripts V1-Vn.
- Business rules:
  - Không dùng hard delete để retain audit history.
  - Seed users/team with sample board & tasks for local dev/testing.
- Dependencies: all domain models.
- Ước lượng độ phức tạp: Medium

---

## 3. Frontend Tasks

### FE-01: Board view và cột Kanban
- Mô tả: Màn hình board chính, hiển thị task theo cột trạng thái, kéo thả task và lọc theo assignee, priority.
- UI/UX cần có:
  - Loading skeleton khi fetch board.
  - Empty state khi board chưa có task.
  - Error state khi API lỗi hoặc board không tồn tại.
  - Drag/drop giữa cột; bàn tay kéo thả; highlight drop zone.
  - Thao tác filter trên header.
- API cần gọi:
  - `GET /api/v1/boards/{boardId}`
  - `GET /api/v1/boards/{boardId}/tasks`
  - `PATCH /api/v1/tasks/{taskId}/move`
- State management:
  - Board data trong TanStack Query cache.
  - Local optimistic update khi drag/drop để UX mượt.
  - Keep `selectedBoardId`, `filters`, `sortOrder` in Zustand.
- Dependencies: BE-01, BE-02, BE-03.
- Ước lượng độ phức tạp: Large

### FE-02: Modal tạo/sửa task
- Mô tả: Form tạo/edit task gồm title, description, assignee, priority, deadline, status.
- UI/UX cần có:
  - Validation inline khi thiếu title / assignee / deadline.
  - Multi-step không cần thiết; form 1 màn hình.
  - Save/Cancel actions.
  - Error banner nếu API reject.
- API cần gọi:
  - `POST /api/v1/tasks`
  - `PUT /api/v1/tasks/{taskId}`
  - `GET /api/v1/users/me`
  - `GET /api/v1/boards/{boardId}/tasks`
- State management:
  - Form state local; mutation state via query invalidation.
  - Cache invalidation for board list and workload summary.
- Dependencies: BE-02, BE-08.
- Ước lượng độ phức tạp: Medium

### FE-03: Task detail drawer / sidebar
- Mô tả: Hiển thị thông tin task, note, timeline và blocker reason. Cho phép edit nhanh.
- UI/UX cần có:
  - Loading fallback khi fetch task detail.
  - Section `Overview`, `Activities`, `Blocker`, `Comments` (nếu có comment ở later phase).
  - Edit action thay cho board lớn.
- API cần gọi:
  - `GET /api/v1/tasks/{taskId}`
  - `PATCH /api/v1/tasks/{taskId}/status`
  - `PATCH /api/v1/tasks/{taskId}/block`
  - `PATCH /api/v1/tasks/{taskId}/unblock`
- State management:
  - Query for task detail.
  - Keep `selectedTaskId` in Zustand for drawer state.
- Dependencies: BE-02, BE-03, BE-07.
- Ước lượng độ phức tạp: Medium

### FE-04: Workload overview & overdue list
- Mô tả: Màn hình hoặc widget dashboard cho manager thấy workload và task quá hạn.
- UI/UX cần có:
  - Card summary: total, overdue, blocked.
  - Table/list theo assignee.
  - Filter theo board/team.
  - Empty state khi không có task.
- API cần gọi:
  - `GET /api/v1/boards/{boardId}/workload`
  - `GET /api/v1/boards/{boardId}/tasks?overdueOnly=true`
- State management:
  - Cache dashboard summary by boardId.
  - Polling interval tùy chọn (30-60s) cho dashboard.
- Dependencies: BE-04.
- Ước lượng độ phức tạp: Medium

### FE-05: Notification center & reminder banner
- Mô tả: Hiển thị thông báo trong app, reminder trước deadline, task sắp hết hạn.
- UI/UX cần có:
  - Notification bell with unread count.
  - Toast for instant delivery of time-sensitive events.
  - Mark as read action.
  - Empty state if no notifications.
- API cần gọi:
  - `GET /api/v1/notifications?userId=7`
  - `PATCH /api/v1/notifications/{notificationId}/read`
- State management:
  - Local notifications state with TanStack Query; unread count derived from data.
  - Optional websocket not required in MVP.
- Dependencies: BE-05.
- Ước lượng độ phức tạp: Small

### FE-06: Dashboard báo cáo tiến độ
- Mô tả: Trang báo cáo tổng quan theo team/board và metric theo thời gian.
- UI/UX cần có:
  - Summary cards and charts.
  - Filter by assignee / team / date range.
  - Export CSV/PDF button (MVP tối thiểu CSV).
- API cần gọi:
  - `GET /api/v1/reports/boards/{boardId}/summary`
  - `GET /api/v1/reports/team/{teamId}?from=...&to=...`
- State management:
  - Query cache for reports.
  - UI filter state in Zustand.
- Dependencies: BE-06.
- Ước lượng độ phức tạp: Medium

### FE-07: Auth, session & role gating
- Mô tả: Quản lý login, role per user, và quyền xem/sửa board/task.
- UI/UX cần có:
  - Login page.
  - Protected routes.
  - Access denied state if user lacks permission.
  - Board list per team.
- API cần gọi:
  - `GET /api/v1/users/me`
  - `GET /api/v1/boards/{boardId}/permissions`
- State management:
  - Auth state in Zustand.
  - Persist session/token in localStorage or HttpOnly cookie depending on auth implementation.
- Dependencies: BE-08.
- Ước lượng độ phức tạp: Medium

### FE-08: Search & filter nhanh
- Mô tả: Tìm task trên board theo title / assignee / status / due date.
- UI/UX cần có:
  - Search input in header.
  - Quick filter pills: overdue, assigned to me, blocked.
- API cần gọi:
  - `GET /api/v1/boards/{boardId}/tasks/search?q=...`
  - `GET /api/v1/boards/{boardId}/tasks`
- State management:
  - Debounced search state.
  - Result list cached per board.
- Dependencies: BE-09.
- Ước lượng độ phức tạp: Small

### FE-09: Board settings & column configuration
- Mô tả: Cho phép Manager tạo/sửa/cập nhật cột board.
- UI/UX cần có:
  - Settings modal hoặc side panel.
  - Add column flow, rename, reorder.
  - Warning if board has tasks in column being removed.
- API cần gọi:
  - `POST /api/v1/boards`
  - `PUT /api/v1/boards/{boardId}`
  - `GET /api/v1/boards/{boardId}`
- State management:
  - Settings mutations invalidates board cache.
  - Undo/rollback not required in MVP.
- Dependencies: BE-01.
- Ước lượng độ phức tạp: Medium

---

## 4. Sequencing đề xuất

### Giai đoạn 1: Foundation (Sprint 1)
- Backend:
  - BE-08: Auth & RBAC
  - BE-10: DB schema + seed data
  - BE-01: Board CRUD
- Frontend:
  - FE-07: Auth & protected routes
  - FE-09: Board settings
- Mục tiêu: có board đầu tiên, user login và quyền hạn cơ bản; không cần feature phức tạp.

### Giai đoạn 2: Core board workflow (Sprint 2)
- Backend:
  - BE-02: Task CRUD
  - BE-03: Task status & move
  - BE-04: Workload summary
- Frontend:
  - FE-01: Board view
  - FE-02: Create/edit task modal
  - FE-03: Task detail drawer
  - FE-04: Workload overview
- Mục tiêu: team có thể làm việc trên board và nhìn thấy tiến độ thực tế.

### Giai đoạn 3: Monitoring & reminders (Sprint 3)
- Backend:
  - BE-05: Notifications
  - BE-07: Blocked reason tracking
  - BE-06: Reporting
- Frontend:
  - FE-05: Notification center
  - FE-06: Dashboard report
  - FE-08: Search/filter
- Mục tiêu: manager có visibility tốt hơn và nhắc việc sớm.

### Parallelism đề xuất
- Backend và Frontend nên chạy song song theo cùng một user story:
  - `Board + schema` → `Board view + settings`
  - `Task CRUD` → `Create task + detail drawer`
  - `Status move` → `Kanban board + drag/drop`
  - `Report/workload` → `Summary dashboard`
- Khuyến nghị: để tránh block, cần đảm bảo contract API đã stable trước khi frontend build UI chính thức. Có thể dùng mock server hoặc contract-first design trong giai đoạn đầu.

---

## 5. Dependencies & risks

### 5.1 Backend risks
- Transaction khi move task đồng thời nhiều người dùng kéo thả cùng lúc.
- Không cho phép duplicate notification trong cùng một trigger.
- `board_members` và `user_roles` cần consistent để không có user ghép sai board/team.
- Workload query cần tránh N+1 khi board lớn.

### 5.2 Frontend risks
- Drag-and-drop library phải đồng bộ tốt với optimistic updates và lỗi API.
- Board dữ liệu lớn cần pagination hoặc virtualized list nếu có >200 task/board.
- Filter + query cache phải tránh stale state khi assignment/status update thay đổi.

---

## 6. Definition of Done (DoD)

- API endpoint có test unit/integration cho happy path và validation failure.
- Frontend có test cho form validation, board render, và error/loading state.
- Sẵn sàng cho QA với dữ liệu demo và một board mẫu.
- Permission check đúng theo role (member/manager/admin).
- Dashboard metrics chính xác với dữ liệu thực tế.

---

## 7. Kết luận

Technical spec này chia feature theo hướng “giải quyết pain point trước” và ưu tiên xây dựng flow cốt lõi của Kanban: board → task → status → load/workload → reminders → report. Với stack đã chọn, team có thể triển khai theo 3 sprint rõ ràng, trong khi tránh over-scope và đảm bảo backend/frontend không bị block nhau.

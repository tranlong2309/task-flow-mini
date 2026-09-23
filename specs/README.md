# Task Flow Specs

## Mục lục

### Backend
- [BE-01: API tạo/cập nhật board](backend/BE-01-board-api.md)
- [BE-02: API quản lý task CRUD](backend/BE-02-task-crud.md)
- [BE-03: API cập nhật trạng thái task & drag/drop](backend/BE-03-task-status-move.md)
- [BE-04: API workload & overdue tracking](backend/BE-04-workload-overdue.md)
- [BE-05: API notification & reminder](backend/BE-05-notification-reminder.md)
- [BE-06: API dashboard/reporting](backend/BE-06-dashboard-reporting.md)
- [BE-07: API block / reason tracking](backend/BE-07-block-tracking.md)
- [BE-08: API auth & RBAC](backend/BE-08-auth-rbac.md)
- [BE-09: API search & filter tasks](backend/BE-09-search-filter.md)
- [BE-10: Database migration & seed data](backend/BE-10-migration-seed.md)

### Frontend
- [FE-01: Board view và cột Kanban](frontend/FE-01-board-view.md)
- [FE-02: Modal tạo/sửa task](frontend/FE-02-task-form.md)
- [FE-03: Task detail drawer / sidebar](frontend/FE-03-task-detail.md)
- [FE-04: Workload overview & overdue list](frontend/FE-04-workload-overview.md)
- [FE-05: Notification center & reminder banner](frontend/FE-05-notification-center.md)
- [FE-06: Dashboard báo cáo tiến độ](frontend/FE-06-report-dashboard.md)
- [FE-07: Auth, session & role gating](frontend/FE-07-auth-role-gating.md)
- [FE-08: Search & filter nhanh](frontend/FE-08-search-filter.md)
- [FE-09: Board settings & column configuration](frontend/FE-09-board-settings.md)

## Cách dùng

- Mỗi file task mô tả 1 unit công việc kỹ thuật rõ ràng cho dev.
- Nhiều task có dependency lẫn nhau; ưu tiên thực hiện theo thứ tự trong file technical spec.
- Mỗi task nên có acceptance criteria rõ để QA và dev review cùng lúc.

## Stack chuẩn

- Backend: Spring Boot 3.x, Java 21, Hexagonal Architecture, MariaDB + Flyway
- Frontend: React + TypeScript, Vite, TanStack Query, Zustand, Tailwind CSS, shadcn/ui

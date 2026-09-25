# BA Analysis - Quan ly Phieu cong viec (Work Order)

## Pham vi va cach hieu

Trong Task Flow Mini, **Work Order** duoc hieu la mot cong viec (`Task`) duoc tao va theo doi tren mot board cua team. Day la don vi cong viec trung tam de giai quyet ba pain point: khong ro ai dang lam gi, task bi tre hoac thieu owner, va manager phai tong hop bao cao thu cong.

Tai lieu nay su dung hai thuat ngu sau:

- **Work Order**: ten nghiep vu theo yeu cau BA.
- **Task**: ten thuc the va resource trong cac dac ta ky thuat hien tai.

MVP uu tien web responsive voi board Kanban; khong phat trien mobile native, khong xay dung chat noi bo, ERP/CRM hoac workflow nhieu dieu kien.

## 1. Danh sach thuc the va thuoc tinh co ban

### 1.1. Task / Work Order

| Thuoc tinh       | Kieu du lieu |         Bat buoc | Quy tac nghiep vu                                                                        |
| ---------------- | ------------ | ---------------: | ---------------------------------------------------------------------------------------- |
| `id`             | Long         |               Co | Khoa chinh, sinh tu dong.                                                                |
| `boardId`        | Long         |               Co | Task phai thuoc mot board ma user co quyen truy cap.                                     |
| `title`          | String       |               Co | Khong null/rong. Dung lam ten ngan cua work order.                                       |
| `description`    | String       |            Khong | Noi dung, scope hoac dau ra can ban giao.                                                |
| `statusColumnId` | Long         |               Co | Cot trang thai hien tai, phai thuoc cung board.                                          |
| `status`         | Enum/String  |               Co | Gia tri hien thi theo workflow, vi du `TODO`, `IN_PROGRESS`, `REVIEW`, `DONE`.           |
| `assigneeId`     | Long         |      Co theo MVP | Owner chiu trach nhiem; phai la thanh vien hop le cua board.                             |
| `priority`       | Enum         |               Co | `LOW`, `MEDIUM`, `HIGH`.                                                                 |
| `dueDate`        | Instant      |      Co theo MVP | Han hoan thanh; luu UTC, hien thi theo timezone cua user.                                |
| `ownerId`        | Long         | Co theo contract | Nguoi chiu trach nhiem theo payload tao task; can thong nhat cach dung voi `assigneeId`. |
| `isBlocked`      | Boolean      |               Co | Mac dinh `false`; task blocked khong duoc coi la hoan thanh.                             |
| `blockedReason`  | String       |   Co khi blocked | Bat buoc neu `isBlocked = true`.                                                         |
| `blockedAt`      | Instant      |   Co khi blocked | Thoi diem task bi block.                                                                 |
| `position`       | Integer      |            Khong | Thu tu task trong mot cot, phuc vu drag/drop.                                            |
| `completedAt`    | Instant      |            Khong | Set khi vao cot `Done`, clear khi roi `Done`.                                            |
| `createdBy`      | Long         |               Co | Lay tu user da xac thuc, khong tin gia tri tu client.                                    |
| `createdAt`      | Instant      |               Co | Thoi diem tao, luu UTC.                                                                  |
| `updatedAt`      | Instant      |               Co | Thoi diem cap nhat gan nhat, luu UTC.                                                    |
| `deletedAt`      | Instant      |            Khong | Soft delete de bao toan audit/history.                                                   |

### 1.2. Board

| Thuoc tinh    | Kieu du lieu | Bat buoc | Quy tac nghiep vu             |
| ------------- | ------------ | -------: | ----------------------------- |
| `id`          | Long         |       Co | Khoa chinh cua board.         |
| `name`        | String       |       Co | Khong rong, toi da 100 ky tu. |
| `description` | String       |    Khong | Mo ta muc dich board/project. |
| `teamId`      | Long         |       Co | Team so huu board.            |
| `createdBy`   | Long         |       Co | User tao board.               |
| `createdAt`   | Instant      |       Co | Thoi diem tao.                |
| `updatedAt`   | Instant      |       Co | Thoi diem cap nhat.           |
| `deletedAt`   | Instant      |    Khong | Soft delete neu duoc ho tro.  |

### 1.3. BoardColumn / Status Column

| Thuoc tinh  | Kieu du lieu | Bat buoc | Quy tac nghiep vu              |
| ----------- | ------------ | -------: | ------------------------------ |
| `id`        | Long         |       Co | Khoa chinh cua cot.            |
| `boardId`   | Long         |       Co | Cot phai thuoc mot board.      |
| `name`      | String       |       Co | Khong rong, toi da 50 ky tu.   |
| `position`  | Integer      |       Co | Thu tu hien thi tren board.    |
| `isDefault` | Boolean      |    Khong | Danh dau cot mac dinh neu can. |
| `createdAt` | Instant      |       Co | Thoi diem tao cot.             |

Moi board can co toi thieu mot cot va toi da 10 cot trong MVP. Neu board co nhieu hon mot cot thi phai co cot `Done`; khong duoc xoa cot dang chua task.

### 1.4. User, Team va Membership

| Thuc the      | Thuoc tinh co ban             | Vai tro nghiep vu                                         |
| ------------- | ----------------------------- | --------------------------------------------------------- |
| `User`        | `id`, `name`, `email`, `role` | Nguoi tao, duoc giao, cap nhat hoac xem task.             |
| `Team`        | `id`, thong tin team          | Pham vi thanh vien va board.                              |
| `BoardMember` | `boardId`, `userId`           | Xac dinh user co duoc truy cap board hay khong.           |
| `TeamMember`  | `teamId`, `userId`            | Co so kiem tra user thuoc team cua board.                 |
| `Role`        | `MEMBER`, `MANAGER`, `ADMIN`  | Quy dinh quyen tao/sua task, quan ly board va xem report. |

Quyen phai duoc kiem tra tai service layer. `ADMIN` co toan quyen; `MANAGER` quan ly board/task/report; `MEMBER` chi tao va chinh sua task cua minh hoac task duoc giao, trong pham vi board duoc phep truy cap.

### 1.5. TaskHistory

| Thuoc tinh  | Kieu du lieu | Bat buoc | Quy tac nghiep vu                               |
| ----------- | ------------ | -------: | ----------------------------------------------- |
| `id`        | Long         |       Co | Khoa chinh.                                     |
| `taskId`    | Long         |       Co | Task duoc thay doi.                             |
| `fieldName` | String       |       Co | Truong thay doi, vi du status, owner, due date. |
| `oldValue`  | String       |    Khong | Gia tri truoc khi thay doi.                     |
| `newValue`  | String       |    Khong | Gia tri sau khi thay doi.                       |
| `changedBy` | Long         |       Co | User thuc hien thay doi.                        |
| `changedAt` | Instant      |       Co | Thoi diem thay doi, luu UTC.                    |

Moi thay doi status, owner/assignee, deadline, block/unblock va thao tac move can duoc ghi nhat ky de phuc vu audit va bao cao.

## 2. Cau hoi con bo ngo va rui ro nghiep vu

| Ma    | Cau hoi / rui ro can lam ro                                                                                                                                 | Muc do   | Owner xac nhan       |
| ----- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- | -------- | -------------------- |
| OQ-01 | Trong contract tao task co ca `assigneeId` va `ownerId`. Hai truong nay co cung nghia hay `ownerId` la nguoi phu trach khac? Truong nao la source of truth? | Critical | PO + Tech Lead       |
| OQ-02 | MVP co bat buoc `dueDate` va assignee hay cho phep tao task chua giao? PRD noi bat buoc, nhung data model hien tai de `assigneeId` nullable.                | High     | PO                   |
| OQ-03 | Ai duoc tao task: `MEMBER`, `MANAGER`, hay chi manager? Quyen MEMBER tao/sua task can gioi han theo creator, assignee hay ca hai?                           | High     | PO + Security        |
| OQ-04 | Board co the co status column tuy chinh. Gia tri `status` trong response co map co dinh voi `TODO/IN_PROGRESS/REVIEW/DONE` hay chi dung ten/id cua column?  | High     | PO + Tech Lead       |
| OQ-05 | Khi user roi team, task dang duoc giao cho user do xu ly the nao: bat buoc reassign, cho phep giu owner cu, hay tu dong chuyen ve unassigned?               | High     | PO                   |
| OQ-06 | Co cho phep mot task co nhieu assignee hay chi mot owner? PRD co de cap giao cho mot hoac nhieu nguoi, trong khi contract chi co mot `assigneeId`.          | High     | PO + Tech Lead       |
| OQ-07 | Khi task bi block, co cho phep dong thoi chuyen vao `Done` khong? Dac ta hien tai noi task blocked khong duoc coi la hoan thanh.                            | High     | PO                   |
| OQ-08 | `blockedAt` do server sinh hay client gui? De dam bao audit, co nen chi chap nhan `reason` va de server ghi thoi diem hien tai?                             | Medium   | Tech Lead + Security |
| OQ-09 | Deadline co luu ca gio/phut va timezone hay chi ngay? Quy tac xu ly task qua han vao ngay deadline can duoc xac nhan.                                       | Medium   | PO                   |
| OQ-10 | Khi hai nguoi move cung task gan dong thoi, quy tac uu tien va xu ly optimistic update/rollback cua UI la gi?                                               | Medium   | PO + Tech Lead       |
| OQ-11 | Soft delete co duoc phep cho task dang co history/blocker khong? Ai duoc khoi phuc hoac xem task da xoa?                                                    | Medium   | PO + Tech Lead       |
| OQ-12 | Reminder truoc deadline co nam trong scope Work Order MVP khong? Neu co, chon moc 1/3/7 ngay va nguoi nhan la assignee, manager hay ca hai?                 | Medium   | PO                   |
| OQ-13 | Search/filter co can phan biet task qua han, blocked, unassigned va status tuy chinh trong mot request khong?                                               | Medium   | PO + UX              |
| OQ-14 | Bao cao can dung du lieu gan thoi gian thuc den muc nao va co yeu cau export CSV/PDF ngay trong MVP khong?                                                  | Low      | PO                   |

### Ranh gioi va rui ro can kiem soat

- Khong duoc xem UI an/khong hien nut la co che phan quyen; backend phai kiem tra authentication, role va board membership.
- Khong duoc nhan `createdBy`, `changedBy` hoac quyen tu request body; lay tu Security Context.
- Truy van task/filter phai dung repository method, parameter binding hoac JPA/PreparedStatement; tuyet doi khong noi chuoi SQL tu input.
- Moi loi validation, khong tim thay resource va khong du quyen phai tra RFC 7807 Problem Details theo quy uoc API.

## 3. Phan ra chi tiet theo mo hinh 3 tang

### 3.1. Tang UI

| Nhom chuc nang  | Man hinh / component             | Du lieu va thao tac                                                                                      | Trang thai / acceptance criteria                                                                  |
| --------------- | -------------------------------- | -------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------- |
| Xem Work Order  | `BoardView`                      | Hien board, cac `BoardColumn` va task trong tung cot; hien title, assignee, priority, due date, blocked. | Loading skeleton, empty state, error state; khong trung task giua cac cot.                        |
| Tao Work Order  | `TaskFormModal`                  | Nhap `title`, `description`, `assigneeId`, `priority`, `dueDate`, `statusColumnId` theo contract.        | Validate inline; thieu title/owner/deadline hien loi; disable Save khi dang submit.               |
| Sua Work Order  | `TaskFormModal` o che do edit    | Sua title, description, assignee, priority, due date.                                                    | Chi hien action neu user co quyen; API tu choi thi hien error banner va giu du lieu local.        |
| Xem chi tiet    | `TaskDetailDrawer`               | Hien overview, status, owner, deadline, blocker va activity/history.                                     | Mo dung task; task khong ton tai co empty/error state.                                            |
| Cap nhat status | Status action trong drawer/board | Chuyen cot bang action ro rang; nhap note khi can.                                                       | Cap nhat thanh cong thi refresh cache; loi thi rollback. Khi vao `Done` hien thi completed state. |
| Drag/drop       | `TaskCard` + drop zone           | Gui source/target column va index qua move API.                                                          | Highlight drop zone; thao tac atomic; khong cho move sang board khac.                             |
| Block/unblock   | Block control trong detail       | Nhap `reason` khi block; cho phep unblock.                                                               | Bat buoc reason; badge blocked va ly do hien ro; task blocked khong hien la completed.            |
| Loc va tim kiem | Filter bar                       | Loc theo `status`, `assigneeId`, `priority`, `search`, `overdueOnly`; co the bo loc.                     | Ket qua khop filter; giu filter trong Zustand, board data trong TanStack Query.                   |
| Quyen truy cap  | Permission-aware actions         | Lay user hien tai va permissions board.                                                                  | UI chi la ho tro UX; moi action van phai duoc backend xac minh.                                   |

### 3.2. Tang Data

| Bang / aggregate                | Cot chinh                                                                                                                                                                                                                 | Constraint / index / transaction                                                                                                                           |
| ------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `boards`                        | `id`, `name`, `description`, `team_id`, `created_by`, timestamps, `deleted_at`                                                                                                                                            | `name` not blank, `team_id` bat buoc; soft delete neu ap dung.                                                                                             |
| `board_columns`                 | `id`, `board_id`, `name`, `position`, `is_default`, `created_at`                                                                                                                                                          | FK ve board; toi thieu 1, toi da 10 cot; index `(board_id, position)`.                                                                                     |
| `tasks`                         | `id`, `board_id`, `title`, `description`, `status_column_id`, `position`, `assignee_id`, `owner_id`, `priority`, `due_date`, `is_blocked`, `blocked_reason`, `blocked_at`, `completed_at`, audit timestamps, `deleted_at` | FK board/column/user; column phai cung board; index cho `(board_id, status_column_id, position)`, assignee, priority, due date; khong coi blocked la Done. |
| `task_history`                  | `id`, `task_id`, `field_name`, `old_value`, `new_value`, `changed_by`, `changed_at`                                                                                                                                       | FK task/user; ghi trong cung transaction voi thay doi task.                                                                                                |
| `users`, `roles`                | `id`, `name`, `email`, role mapping                                                                                                                                                                                       | Email/username duy nhat theo auth design; khong luu password plaintext.                                                                                    |
| `team_members`, `board_members` | `team_id/user_id`, `board_id/user_id`                                                                                                                                                                                     | Unique cap doi; dung de kiem tra pham vi truy cap.                                                                                                         |

Quy tac du lieu chung:

- Timestamp luu UTC; API dung ISO-8601, vi du `2026-09-30T17:00:00Z`.
- Update status/move, assignment, deadline va block/unblock phai ghi history tuong ung.
- Move task phai la transaction atomic; khi vao `Done` set `completed_at`, khi roi `Done` thi clear.
- Soft delete khong xoa history de giu audit.
- Moi gia tri tu nguoi dung phai duoc validate o application boundary; truy van dung parameter binding/JPA.

### 3.3. Tang API

| Method  | Endpoint                               | Muc dich / payload chinh                                                                                          | Quyen va response mong doi                                                            |
| ------- | -------------------------------------- | ----------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------- |
| `POST`  | `/api/v1/tasks`                        | Tao task voi `boardId`, `title`, `description`, `assigneeId`, `priority`, `dueDate`, `statusColumnId`, `ownerId`. | User co quyen tao task va thuoc board; `201 Created`.                                 |
| `PUT`   | `/api/v1/tasks/{taskId}`               | Cap nhat task voi title, description, assignee, priority, due date.                                               | Owner/assignee phu hop, manager hoac admin; `200 OK`; ghi history cho field thay doi. |
| `GET`   | `/api/v1/tasks/{taskId}`               | Lay chi tiet task.                                                                                                | User co board membership; `200 OK`, `404` neu khong ton tai.                          |
| `GET`   | `/api/v1/boards/{boardId}/tasks`       | Lay task theo board; filter `status`, `assigneeId`, `priority`, `search`, `overdueOnly`.                          | User co quyen xem board; response phan trang/envelope theo API rule.                  |
| `PATCH` | `/api/v1/tasks/{taskId}/status`        | Cap nhat `statusColumnId`, `note`, `blockedReason` theo status contract.                                          | Member phu hop, manager/admin; validate column cung board; `200 OK`.                  |
| `PATCH` | `/api/v1/tasks/{taskId}/move`          | Cap nhat vi tri voi `sourceColumnId`, `targetColumnId`, `sourceIndex`, `targetIndex`.                             | User co quyen sua task; transaction atomic; `200 OK`.                                 |
| `PATCH` | `/api/v1/tasks/{taskId}/block`         | Set `reason` va trang thai blocked.                                                                               | Assignee, manager/admin; reason bat buoc; `200 OK`.                                   |
| `PATCH` | `/api/v1/tasks/{taskId}/unblock`       | Bo trang thai blocked.                                                                                            | Assignee, manager/admin; `200 OK`.                                                    |
| `GET`   | `/api/v1/boards/{boardId}`             | Lay board va cac status column.                                                                                   | User co board membership; `200 OK`.                                                   |
| `GET`   | `/api/v1/users/me`                     | Lay user, role, teams de hien thi dung context.                                                                   | User da xac thuc; `200 OK`.                                                           |
| `GET`   | `/api/v1/boards/{boardId}/permissions` | Lay quyen UI theo board.                                                                                          | User da xac thuc; backend van enforce quyen tren moi mutation.                        |

API va validation bat buoc:

- Dung prefix `/api/v1`, plural noun va kebab-case; khong dung verb trong URI.
- Request body phai duoc `@Valid`; dung `@NotBlank`, `@NotNull`, `@Size` va validation enum/pham vi phu hop.
- Loi tra RFC 7807 Problem Details voi `type`, `title`, `status`, `detail`; dung `400`, `401`, `403`, `404`, `500` dung ngu canh.
- Khong tra raw list/raw primitive; danh sach task phai nam trong response object co typed fields va total/pagination theo contract.
- Khong cho client tu ghi de `createdBy`, `changedBy`, role hoac board membership.

### Tieu chi nghiem thu lien thong ba tang

1. User co quyen tao Work Order voi title, owner/assignee, priority va deadline; task hien ngay tren dung board/cot sau khi save.
2. Task khong co owner hoac deadline bi chan theo quy tac MVP va hien validation ro rang.
3. Drag/drop va cap nhat status cap nhat UI, server, `position`, `completedAt` va `task_history` mot cach nhat quan.
4. Block task khong co reason bi tu choi; task blocked hien tren detail, board va dashboard.
5. User khong thuoc board hoac khong co role phu hop nhan `403`, ke ca khi tu goi API truc tiep.
6. Filter/search khong lam lo task cua board khac va khong tao truy van SQL bang noi chuoi input.

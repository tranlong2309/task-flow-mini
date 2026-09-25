# Code Review - TaskController

**Reviewer role:** Senior Solution Architect and Security Code Reviewer
**Review scope:** Quan ly Work Order/Task trong Task Flow Mini
**File reviewed:** `backend/src/main/java/com/taskflow/infrastructure/web/controller/TaskController.java`
**Review date:** 2026-09-25
**Reference:** `docs/api-rules.md`, `docs/security-rules.md`, `TASK_FLOW_REQUIREMENT.md`, `TASK_FLOW_TECHNICAL_SPEC.md`

## Pham vi review

Du an khong co `WorkOrderController`, `equipment_id`, `TECHNICIAN/SUPERVISOR` hoac endpoint `/api/work-orders`. Trong Task Flow Mini, Work Order duoc hien thuc bang `Task` tren board Kanban. Review nay danh gia controller hien co va cac use case truc tiep ma controller goi.

## Diem tot

- Controller da dung base path `/api/v1` va resource path dang so nhieu `/tasks`.
- Controller lay user hien tai tu `@AuthenticationPrincipal CustomUserDetails`, khong nhan `createdBy` tu request.
- Logic nghiep vu duoc day vao application use case thay vi truy cap `DataSource`/SQL truc tiep.
- Persistence dang dung Spring Data JPA repository, phu hop yeu cau phong SQL Injection.
- Tao task tra `201 Created`; cac mutation status, move, block va unblock dung `PATCH`.
- Application service da co cac kiem tra board permission va assignee membership o mot so flow.

## Findings theo muc do

### [P1 - API CONTRACT] CR-01: Request body dung `Map<String, Object>`, khong co DTO va `@Valid`

**Vi tri:** `TaskController#createTask`, `updateTask`, `updateStatus`, `moveTask`, `blockTask`

**Van de:** Controller parse thu cong payload bang cast, `toString()`, `Priority.valueOf` va `Instant.parse`. Cach nay khong tuan thu `docs/api-rules.md` Rule 5 va tao cac loi khong nhat quan:

- Thieu field co the gay `NullPointerException` thay vi validation error `400`.
- Sai kieu du lieu co the gay `ClassCastException`.
- Enum/date sai format khong duoc tra ve theo Problem Details.
- Khong co gioi han do dai cho title, description, note, reason va cac input string.

**Khuyen nghi:** Tao request DTO rieng cho tung contract, dung `@Valid` va Bean Validation:

```java
public record CreateTaskRequest(
        @NotNull UUID boardId,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 5000) String description,
        Long assigneeId,
        @NotNull Priority priority,
        @NotNull Instant dueDate,
        @NotNull Long statusColumnId
) {}
```

Controller chi nhan DTO da validate va map sang use case. Khong cho client tu gui `createdBy`, role, permission hoac cac truong audit.

### [P1 - ERROR CONTRACT] CR-02: Error response khong theo RFC 7807 va map sai HTTP status

**Vi tri:** Cac `catch (IllegalArgumentException e)` va cac response `Map.of("error", ...)`

**Van de:** API rules yeu cau error response co `type`, `title`, `status`, `detail`. Hien tai controller tra raw map chi co `error`. Ngoai ra:

- Task khong ton tai dang bi tra `400` trong `getTask`, trong khi contract yeu cau `404`.
- `AccessDeniedException` khong duoc xu ly nhat quan; mot so endpoint co the roi vao `500` thay vi `403` neu global handler khong bao phu.
- Cac loi parse payload va loi runtime khong duoc map ro rang.

**Khuyen nghi:** Dung `@RestControllerAdvice` tap trung de map `MethodArgumentNotValidException`, `IllegalArgumentException`, resource-not-found exception va `AccessDeniedException` sang `ProblemDetail`/RFC 7807. Controller khong nen tu tao error map o tung method.

### [P1 - AUDIT] CR-03: Khong ghi day du `task_history` cho cac thay doi task

**Vi tri:** `TaskApplicationService#updateTask`

**Van de:** Service chi goi `recordHistory` khi thay doi `assigneeId` va `statusColumnId`. Theo business rules, moi thay doi deadline, owner/assignee va status phai co history; de dam bao audit va bao cao, thay doi priority va cac truong quan trong cung can quy tac ro rang. Hien tai cac thay doi `priority` va `dueDate` duoc save nhung khong ghi history.

**Khuyen nghi:** Truoc moi mutation, luu old/new value trong cung transaction voi task update. Toi thieu can bao phu `assignee_id`, `status_column_id`, `priority`, `due_date`, `title`, `description` neu day la cac truong duoc audit theo requirement. Them test xac nhan update khong the thanh cong neu history khong duoc ghi.

### [P1 - DATA INTEGRITY] CR-04: Chua thay doi `completedAt` khi chuyen vao/ra cot Done

**Vi tri:** `TaskApplicationService#updateTask`; can doi chieu them `TaskStatusApplicationService` va `MoveTaskApplicationService`

**Van de:** Contract BE-03 yeu cau khi task vao `Done` phai set `completedAt`, khi roi `Done` phai clear. Trong flow update task dang thay doi `statusColumnId` va ghi history nhung khong thay logic cap nhat `completedAt`. Neu status flow/move service cung khong xu ly, workload va overdue se sai.

**Khuyen nghi:** Tap trung quy tac status transition trong mot application service/domain policy. Xac dinh cot Done theo board, cap nhat `completedAt` trong cung transaction, va viet test cho ca hai chieu `Done`/`non-Done`.

### [P1 - AUTHORIZATION] CR-05: Thieu enforcement ro rang tai controller; phu thuoc hoan toan vao service

**Vi tri:** Tat ca endpoint trong `TaskController`

**Van de:** Controller co `AuthenticationPrincipal`, nhung khong co `@PreAuthorize` hoac annotation tuong duong. Service da co kiem tra permission trong mot so flow, day la diem tot, nhung security rule yeu cau server-side authorization tren moi endpoint nhay cam. Can dam bao ca cac use case status/move/block/unblock/search deu kiem tra board membership va role, khong chi task CRUD.

**Khuyen nghi:** Chon mot chien luoc nhat quan:

- Dung `@PreAuthorize` cho cac quyen role co the mo ta o controller va giu object-level check trong service; hoac
- Ghi ro policy o application service va bao phu bang integration tests cho MEMBER/MANAGER/ADMIN, user ngoai board va assignee khong hop le.

Khong coi permission UI hoac `GET /permissions` la security boundary.

### [P2 - SCHEMA] CR-06: Tra truc tiep domain `Task` ra HTTP response

**Vi tri:** `return ResponseEntity.ok(task)` va `body(task)`

**Van de:** API rules yeu cau schema response nghiem ngat. Tra domain object truc tiep lam API phu thuoc vao getter/field noi bo, de vo tinh lo truong audit hoac thay doi public contract khi domain thay doi. Response create/update/detail cung chua duoc phan tach ro theo contract.

**Khuyen nghi:** Tao response DTO rieng cho create, detail, status/move va list. Chi expose cac field da duoc contract phe duyet; map o adapter/controller layer.

### [P2 - INPUT BOUNDARY] CR-07: Pagination va query input chua duoc validate day du

**Vi tri:** `searchTasks`, `searchTasksAdvanced`

**Van de:** Code chi gioi han `size > 100`, nhung khong chan `size <= 0`, `page < 0` hoac input search qua dai o endpoint thong thuong. Dieu nay co the gay loi repository, truy van khong hop ly hoac lam tang tai he thong.

**Khuyen nghi:** Dung `Pageable` voi whitelist/max size va DTO/query object co validation. Gioi han do dai `search` o ca hai endpoint, normalize input va reject gia tri khong hop le bang `400 Problem Details`.

### [P2 - CONSISTENCY] CR-08: Hai endpoint search co contract va cach xu ly khac nhau

**Vi tri:** `GET /api/v1/boards/{boardId}/tasks` va `GET /api/v1/boards/{boardId}/tasks/search`

**Van de:** Hai API cung phuc vu tim task nhung dung query khac nhau (`search` so voi `q`, `statusColumnId` so voi `status`), response khac nhau va xu ly loi khac nhau. Endpoint search advanced con load columns truoc khi use case thuc hien permission check.

**Khuyen nghi:** PO/Tech Lead chot mot contract search duy nhat trong BE-09/FE-08. Neu giu ca hai endpoint, phai quy dinh ro muc dich, response envelope va auth check truoc moi truy cap du lieu board. Khong de controller tu map ten column thanh magic fallback `-1L`.

### [P2 - TRANSACTION] CR-09: Can xac minh atomicity cua status/move va history

**Vi tri:** `TaskController#updateStatus`, `moveTask`; cac use case tuong ung

**Van de:** Controller chi forward request va khong the hien transaction boundary. BE-03 yeu cau move atomic, cap nhat position dong bo va ghi history. Neu transaction chi dat o mot service khac hoac thieu lock, hai request drag/drop dong thoi co the lam sai position.

**Khuyen nghi:** Dat `@Transactional` tai application service boundary, dung locking/optimistic versioning phu hop va test concurrent move. Moi thay doi task, position, completedAt va history phai commit cung nhau.

### [P3 - MAINTAINABILITY] CR-10: Controller dang chua qua nhieu parsing va fully-qualified type

**Vi tri:** Toan bo `TaskController`

**Van de:** Controller co parsing, status lookup, response mapping va exception mapping; dong thoi su dung wildcard import va nhieu fully-qualified class trong method. Dieu nay lam tang chi phi bao tri va kho review contract.

**Khuyen nghi:** Tach request/response mapper, query object va exception handler; import class day du va giu controller mong, chi dieu phoi HTTP boundary.

## Cac diem can xac minh them

- Security filter chain co bat buoc authentication cho `/api/v1/tasks/**` va `/api/v1/boards/**` hay khong.
- `TaskStatusApplicationService`, `MoveTaskApplicationService`, `BlockTaskApplicationService` co ghi history, enforce same-board column va cap nhat `completedAt` day du hay khong.
- Global exception handler co map `AccessDeniedException`, validation exception va not-found exception dung `401/403/404` hay khong.
- Contract co chap nhan `assigneeId = null` de unassign hay khong; implementation hien tai khong cho phep clear assignee vi chi xu ly khi gia tri khac null.
- `ownerId` trong technical spec chua duoc controller/service su dung; PO/Tech Lead can chot owner va assignee la mot hay hai khai niem.

## Tong ket

| Nhom                         | So luong | Muc do chinh |
| ---------------------------- | -------: | ------------ |
| API contract va validation   |        2 | P1           |
| Audit va data integrity      |        2 | P1           |
| Authorization va transaction |        2 | P1/P2        |
| Schema/search consistency    |        3 | P2           |
| Maintainability              |        1 | P3           |

### Thu tu xu ly de xuat

1. Thay `Map<String, Object>` bang DTO co `@Valid` va chuan hoa Problem Details/HTTP status.
2. Dam bao authorization, status transition, `completedAt`, position va history duoc xu ly atomic trong application service.
3. Tach response DTO, chot contract search va bo sung integration tests cho RBAC/IDOR.
4. Sau do refactor controller va them validation cho pagination/query.

### Pham vi khong ap dung

Khong co finding ve hardcoded secret, equipment-specific fields, raw JDBC statement hay SQL string concatenation trong source hien tai. Chi ghi nhan cac van de co that trong repository va khong dua them cac finding cua module ngoai pham vi.

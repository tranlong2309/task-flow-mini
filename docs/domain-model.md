# Domain Model — Task Flow Mini (Work Order)

> **Section 5 – Day 3 | Spec-Driven Design (SDD)**  
> Tài liệu này mô tả Domain Model cho hệ thống **Work Order** (Task) của Task Flow Mini.  
> _Không có mã nguồn nào được viết trước khi tài liệu này được phê duyệt (Peer Review)._

---

## 1. Bounded Context

```
┌─────────────────────────────────────────────────────────────────────┐
│                      Task Flow Mini                                  │
│                                                                     │
│  ┌──────────────────┐     ┌──────────────────┐                      │
│  │  Board Context   │ ──► │  Work Order (WO) │                      │
│  │  (Quản lý Board) │     │  Context         │                      │
│  └──────────────────┘     └──────────────────┘                      │
│           │                        │                                │
│           ▼                        ▼                                │
│  ┌──────────────────┐     ┌──────────────────┐                      │
│  │  Identity &      │     │  Notification    │                      │
│  │  Access (Auth)   │     │  Context         │                      │
│  └──────────────────┘     └──────────────────┘                      │
└─────────────────────────────────────────────────────────────────────┘
```

Context trọng tâm của Day 3 là **Work Order Context** — bao gồm toàn bộ vòng đời của một Work Order (Task) từ khi tạo đến khi hoàn thành hoặc bị xoá mềm.

---

## 2. Aggregate Root: WorkOrder

```
WorkOrder (Aggregate Root)
├── id                 : WorkOrderId          [Value Object]
├── boardId            : BoardId              [Value Object / FK]
├── title              : WorkOrderTitle       [Value Object]
├── description        : String               [nullable]
├── status             : WorkOrderStatus      [Enum / Value Object]
├── statusColumnId     : BoardColumnId        [Value Object / FK]
├── priority           : Priority             [Enum]
├── dueDate            : DueDate              [Value Object]
├── assigneeId         : UserId               [Value Object / FK]
├── ownerId            : UserId               [Value Object / FK]
├── position           : Integer              [ordering in column]
├── isBlocked          : Boolean
├── blockedReason      : BlockedReason        [Value Object, nullable]
├── blockedAt          : Instant              [nullable]
├── completedAt        : Instant              [nullable]
├── audit              : AuditInfo            [Value Object]
│   ├── createdBy      : UserId
│   ├── createdAt      : Instant
│   ├── updatedAt      : Instant
│   └── deletedAt      : Instant              [nullable, soft delete]
└── history            : List<WorkOrderHistory> [Entity — child]
```

### Bất biến (Invariants)

| # | Invariant | Mô tả |
|---|-----------|-------|
| I-01 | Title không rỗng | `title` bắt buộc, không null/blank, tối đa 255 ký tự. |
| I-02 | Board membership | `assigneeId` phải là thành viên hợp lệ của board được khai báo bởi `boardId`. |
| I-03 | Column cùng board | `statusColumnId` phải thuộc cùng board với `boardId`. |
| I-04 | Due date bắt buộc | Trong MVP, `dueDate` bắt buộc khi tạo Work Order active. |
| I-05 | Blocked → không Done | Work Order có `isBlocked = true` không được chuyển sang cột `Done`. |
| I-06 | Blocked reason bắt buộc | Khi `isBlocked = true`, `blockedReason` không được null/rỗng. |
| I-07 | completedAt lifecycle | `completedAt` chỉ được set khi vào cột `Done`; bị clear khi rời `Done`. |
| I-08 | Soft delete | Xoá Work Order chỉ set `deletedAt`; history và audit không bị mất. |
| I-09 | createdBy từ server | `createdBy` và `changedBy` lấy từ Security Context, không chấp nhận từ client. |

---

## 3. Entities

### 3.1 WorkOrderHistory (Entity — child of WorkOrder)

Ghi lại mọi thay đổi trạng thái, assignee, deadline và block/unblock để phục vụ audit và báo cáo.

```
WorkOrderHistory
├── id          : Long            [PK, auto-generated]
├── workOrderId : WorkOrderId     [FK → WorkOrder]
├── fieldName   : String          [tên trường thay đổi, vd: "status", "assigneeId"]
├── oldValue    : String          [nullable — giá trị trước]
├── newValue    : String          [nullable — giá trị sau]
├── changedBy   : UserId          [FK → User, từ Security Context]
└── changedAt   : Instant         [UTC, sinh bởi server]
```

**Quy tắc**: Mọi thao tác thay đổi `status`, `move`, `assigneeId`, `dueDate`, `block/unblock` đều phải ghi một bản ghi `WorkOrderHistory` trong cùng transaction.

---

## 4. Value Objects

### 4.1 WorkOrderId
```
WorkOrderId
└── value : Long   [PK, auto-increment, > 0]
```

### 4.2 WorkOrderTitle
```
WorkOrderTitle
└── value : String   [NotBlank, max 255 chars]
```
_Invariant_: Không được null, không được blank sau khi trim.

### 4.3 DueDate
```
DueDate
└── value : Instant   [UTC, phải >= thời điểm tạo WO]
```
_Invariant_: Không được là thời điểm trong quá khứ khi tạo mới; lưu UTC, hiển thị theo timezone người dùng.

### 4.4 Priority (Enum)
```
Priority
├── LOW
├── MEDIUM
└── HIGH
```

### 4.5 WorkOrderStatus (Enum / Column Mapping)
```
WorkOrderStatus
├── TODO         → cột "Todo"
├── IN_PROGRESS  → cột "In Progress"
├── REVIEW       → cột "Review"
├── DONE         → cột "Done"
└── BLOCKED      → overlay state (isBlocked = true), không thay thế column
```
_Lưu ý_: `status` là derived value từ `statusColumnId`. Giá trị enum có thể được hiển thị theo tên cột tuỳ chỉnh của board (xem OQ-04 trong br-analysis-wo.md).

### 4.6 BlockedReason
```
BlockedReason
└── value : String   [NotBlank khi isBlocked = true, max 500 chars]
```

### 4.7 AuditInfo (Embedded Value Object)
```
AuditInfo
├── createdBy  : UserId
├── createdAt  : Instant   [UTC]
├── updatedAt  : Instant   [UTC]
└── deletedAt  : Instant   [nullable, UTC — soft delete marker]
```

---

## 5. Domain Events

| Event | Trigger | Payload chính |
|-------|---------|---------------|
| `WorkOrderCreated` | Tạo mới Work Order thành công | `workOrderId`, `boardId`, `assigneeId`, `dueDate`, `createdBy` |
| `WorkOrderStatusChanged` | Chuyển cột (kể cả move) | `workOrderId`, `fromColumnId`, `toColumnId`, `changedBy`, `changedAt` |
| `WorkOrderCompleted` | Vào cột Done | `workOrderId`, `completedAt`, `changedBy` |
| `WorkOrderBlocked` | Set `isBlocked = true` | `workOrderId`, `blockedReason`, `blockedAt`, `changedBy` |
| `WorkOrderUnblocked` | Set `isBlocked = false` | `workOrderId`, `unblockedAt`, `changedBy` |
| `WorkOrderAssigned` | Thay đổi `assigneeId` | `workOrderId`, `oldAssigneeId`, `newAssigneeId`, `changedBy` |
| `WorkOrderDeleted` | Soft delete | `workOrderId`, `deletedAt`, `deletedBy` |

_Domain Events được publish sau khi commit transaction thành công; dùng để trigger Notification Context._

---

## 6. Supporting Entities (tham chiếu từ các Context khác)

### 6.1 Board (Reference — Board Context)
```
Board [Aggregate Root — Board Context]
├── id          : BoardId
├── name        : String       [NotBlank, max 100 chars]
├── description : String       [nullable]
├── teamId      : TeamId
└── columns     : List<BoardColumn>
```

### 6.2 BoardColumn (Entity — Board Context)
```
BoardColumn
├── id        : BoardColumnId
├── boardId   : BoardId
├── name      : String       [NotBlank, max 50 chars]
├── position  : Integer      [≥ 1]
└── isDefault : Boolean
```
_Ràng buộc_: Mỗi board tối thiểu 1 cột, tối đa 10 cột. Bắt buộc có cột `Done` nếu board có nhiều hơn 1 cột. Không xoá cột đang chứa Work Order.

### 6.3 User (Reference — Identity Context)
```
User [Reference]
├── id    : UserId
├── name  : String
├── email : String
└── role  : Role [MEMBER | MANAGER | ADMIN]
```

---

## 7. Aggregate Relationship Diagram

```
Board Context                    Work Order Context
─────────────────                ─────────────────────────────────────
Board (AR)                       WorkOrder (AR)
  └── BoardColumn (Entity)  ◄──── statusColumnId
                                  ├── WorkOrderHistory (Entity)
                                  └── [Domain Events]

Identity Context
─────────────────
User (AR)
  └── Role (Enum)          ◄──── assigneeId / ownerId / createdBy
```

---

## 8. Vòng đời Work Order (State Machine)

```
[CREATE]
    │
    ▼
 TODO ──────────────────────────────────────────────────►[DELETED]
    │                                                       ▲
    ▼                                                       │
IN_PROGRESS ──────────────────────────────────────────────►│
    │           ▲                                           │
    ▼           │                                           │
 REVIEW ────────┘ (back to IN_PROGRESS)                    │
    │                                                       │
    ▼                                                       │
 DONE ──────────────────────────────────────────────────►──┘

[BLOCKED] = overlay state trên bất kỳ bước nào (trừ DONE)
           → isBlocked = true, reason bắt buộc
           → Không thể chuyển sang DONE khi đang BLOCKED
```

**Quy tắc chuyển trạng thái**:
- Từ bất kỳ cột nào → cột khác: hợp lệ nếu column thuộc cùng board và user có quyền.
- `BLOCKED` → `DONE`: **bị từ chối** (I-05).
- Vào `Done`: server tự set `completedAt = now()`.
- Rời `Done` (reopen): server tự clear `completedAt = null`.

---

## 9. Phân quyền theo Domain

| Action | MEMBER | MANAGER | ADMIN |
|--------|--------|---------|-------|
| Xem Work Order (board member) | ✅ | ✅ | ✅ |
| Tạo Work Order | ✅ | ✅ | ✅ |
| Sửa Work Order (của mình hoặc được giao) | ✅ | ✅ | ✅ |
| Sửa Work Order (của người khác) | ❌ | ✅ | ✅ |
| Chuyển trạng thái / Move | ✅ (assigned) | ✅ | ✅ |
| Block / Unblock | ✅ (assignee) | ✅ | ✅ |
| Xoá Work Order (soft delete) | ❌ | ✅ | ✅ |
| Xem lịch sử (History) | ✅ | ✅ | ✅ |

---

## 10. Glossary

| Thuật ngữ | Định nghĩa |
|-----------|------------|
| **Work Order (WO)** | Tên nghiệp vụ, tương đương `Task` trong codebase. |
| **Aggregate Root** | Điểm vào duy nhất để thay đổi state của aggregate; mọi thao tác phải qua AR. |
| **Value Object** | Đối tượng không có identity; so sánh bằng giá trị; bất biến sau khi tạo. |
| **Domain Event** | Sự kiện quan trọng đã xảy ra trong domain; dùng để tích hợp giữa các context. |
| **Soft Delete** | Xoá logic bằng cách set `deletedAt`; không xoá vật lý để bảo toàn audit. |
| **Board Member** | User thuộc board; là điều kiện bắt buộc để truy cập Work Order trong board. |
| **Blocked State** | Trạng thái phủ (overlay) — Work Order bị chặn bởi dependency bên ngoài. |

---

> **Peer Review Checklist**
> - [ ] Domain Model phản ánh đúng các User Story trong PRD (Story 2, 3, 4, 5, 7, 10).
> - [ ] Tất cả Invariants được liệt kê và không mâu thuẫn với Business Rules trong `br-analysis-wo.md`.
> - [ ] Value Objects rõ ràng, tách biệt khỏi primitive types.
> - [ ] Domain Events đủ để trigger Notification Context.
> - [ ] State Machine phù hợp với `WorkOrderStatus` và `isBlocked`.
> - [ ] Phân quyền khớp với RBAC trong `BE-08` của `TASK_FLOW_TECHNICAL_SPEC.md`.

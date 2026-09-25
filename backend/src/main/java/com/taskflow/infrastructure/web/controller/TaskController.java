package com.taskflow.infrastructure.web.controller;

import com.taskflow.application.port.in.CreateTaskUseCase;
import com.taskflow.application.port.in.DeleteTaskUseCase;
import com.taskflow.application.port.in.GetTaskUseCase;
import com.taskflow.application.port.in.SearchTasksUseCase;
import com.taskflow.application.port.in.UpdateTaskUseCase;
import com.taskflow.application.port.in.UpdateTaskStatusUseCase;
import com.taskflow.application.port.in.MoveTaskUseCase;
import com.taskflow.application.port.in.BlockTaskUseCase;
import com.taskflow.application.port.in.UnblockTaskUseCase;
import com.taskflow.domain.model.BoardColumn;
import com.taskflow.domain.model.Priority;
import com.taskflow.domain.model.Task;
import com.taskflow.infrastructure.security.CustomUserDetails;
import com.taskflow.infrastructure.web.dto.ProblemDetail;
import com.taskflow.infrastructure.web.dto.PagedWorkOrderResponse;
import com.taskflow.infrastructure.web.dto.WorkOrderResponse;
import com.taskflow.infrastructure.web.dto.WorkOrderSummary;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.Set;

/**
 * TaskController — thin controller theo spec docs/api-spec.yaml.
 *
 * Spec violations đã fix:
 * - Trả WorkOrderResponse DTO thay vì Task entity trực tiếp.
 * - Error response theo RFC 7807 ProblemDetail.
 * - Phân biệt 400 (validation) vs 403 (permission) vs 404 (not found) vs 500.
 */
@RestController
@RequestMapping("/api/v1")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    private final CreateTaskUseCase createTaskUseCase;
    private final UpdateTaskUseCase updateTaskUseCase;
    private final GetTaskUseCase getTaskUseCase;
    private final SearchTasksUseCase searchTasksUseCase;
    private final DeleteTaskUseCase deleteTaskUseCase;
    private final UpdateTaskStatusUseCase updateTaskStatusUseCase;
    private final MoveTaskUseCase moveTaskUseCase;
    private final BlockTaskUseCase blockTaskUseCase;
    private final UnblockTaskUseCase unblockTaskUseCase;
    private final com.taskflow.domain.repository.BoardColumnRepositoryPort boardColumnRepositoryPort;

    public TaskController(CreateTaskUseCase createTaskUseCase,
                          UpdateTaskUseCase updateTaskUseCase,
                          GetTaskUseCase getTaskUseCase,
                          SearchTasksUseCase searchTasksUseCase,
                          DeleteTaskUseCase deleteTaskUseCase,
                          UpdateTaskStatusUseCase updateTaskStatusUseCase,
                          MoveTaskUseCase moveTaskUseCase,
                          BlockTaskUseCase blockTaskUseCase,
                          UnblockTaskUseCase unblockTaskUseCase,
                          com.taskflow.domain.repository.BoardColumnRepositoryPort boardColumnRepositoryPort) {
        this.createTaskUseCase = createTaskUseCase;
        this.updateTaskUseCase = updateTaskUseCase;
        this.getTaskUseCase = getTaskUseCase;
        this.searchTasksUseCase = searchTasksUseCase;
        this.deleteTaskUseCase = deleteTaskUseCase;
        this.updateTaskStatusUseCase = updateTaskStatusUseCase;
        this.moveTaskUseCase = moveTaskUseCase;
        this.blockTaskUseCase = blockTaskUseCase;
        this.unblockTaskUseCase = unblockTaskUseCase;
        this.boardColumnRepositoryPort = boardColumnRepositoryPort;
    }

    // ─── POST /api/v1/tasks ────────────────────────────────────────────────────
    // Spec: docs/api-spec.yaml → POST /work-orders → 201 Created
    @PostMapping("/tasks")
    public ResponseEntity<?> createTask(@RequestBody Map<String, Object> payload,
                                        @AuthenticationPrincipal CustomUserDetails userDetails,
                                        HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        try {
            UUID boardId = UUID.fromString(payload.get("boardId").toString());
            String title = (String) payload.get("title");
            String description = (String) payload.get("description");

            Set<Long> assigneeIds = new java.util.HashSet<>();
            if (payload.get("assigneeIds") != null) {
                List<?> list = (List<?>) payload.get("assigneeIds");
                for (Object o : list) {
                    assigneeIds.add(Long.valueOf(o.toString()));
                }
            } else if (payload.get("assigneeId") != null) {
                assigneeIds.add(Long.valueOf(payload.get("assigneeId").toString()));
            }

            Priority priority = Priority.valueOf((String) payload.get("priority"));
            Instant dueDate = payload.get("dueDate") != null
                    ? Instant.parse(payload.get("dueDate").toString()) : null;
            Long statusColumnId = payload.get("statusColumnId") != null
                    ? Long.valueOf(payload.get("statusColumnId").toString()) : null;

            Task task = createTaskUseCase.createTask(
                    boardId, title, description, assigneeIds, priority, dueDate, statusColumnId,
                    userDetails.getId());

            // Fix: trả DTO thay vì entity (spec: WorkOrderResponse)
            String columnName = resolveColumnName(task.getStatusColumnId());
            WorkOrderResponse dto = WorkOrderResponse.from(task, columnName);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("Location", "/api/v1/tasks/" + dto.id())
                    .body(dto);

        } catch (AccessDeniedException e) {
            // Fix: RFC 7807 403 thay vì raw Map
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.forbidden(e.getMessage(), requestUri));
        } catch (IllegalArgumentException e) {
            // Fix: RFC 7807 400 thay vì raw Map
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.badRequest(e.getMessage(), requestUri));
        } catch (Exception e) {
            String trackingId = "err-" + System.currentTimeMillis();
            log.error("[{}] Unexpected error in createTask: {}", trackingId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.internalServerError(trackingId, requestUri));
        }
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<?> updateTask(@PathVariable UUID taskId,
                                        @RequestBody Map<String, Object> payload,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            String title = payload.containsKey("title") ? (String) payload.get("title") : null;
            String description = payload.containsKey("description") ? (String) payload.get("description") : null;
            
            Set<Long> assigneeIds = null;
            if (payload.containsKey("assigneeIds") && payload.get("assigneeIds") != null) {
                assigneeIds = new java.util.HashSet<>();
                List<?> list = (List<?>) payload.get("assigneeIds");
                for (Object o : list) {
                    assigneeIds.add(Long.valueOf(o.toString()));
                }
            } else if (payload.containsKey("assigneeId") && payload.get("assigneeId") != null) {
                assigneeIds = new java.util.HashSet<>();
                assigneeIds.add(Long.valueOf(payload.get("assigneeId").toString()));
            }

            Priority priority = payload.containsKey("priority") ? Priority.valueOf((String) payload.get("priority")) : null;
            Instant dueDate = payload.containsKey("dueDate") && payload.get("dueDate") != null ? Instant.parse(payload.get("dueDate").toString()) : null;
            Instant assignedDate = payload.containsKey("assignedDate") && payload.get("assignedDate") != null ? Instant.parse(payload.get("assignedDate").toString()) : null;
            Instant startDate = payload.containsKey("startDate") && payload.get("startDate") != null ? Instant.parse(payload.get("startDate").toString()) : null;
            Long statusColumnId = payload.containsKey("statusColumnId") && payload.get("statusColumnId") != null ? Long.valueOf(payload.get("statusColumnId").toString()) : null;
            
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            
            List<com.taskflow.domain.model.Subtask> subtasks = null;
            if (payload.containsKey("subtasks") && payload.get("subtasks") != null) {
                subtasks = mapper.convertValue(payload.get("subtasks"), new com.fasterxml.jackson.core.type.TypeReference<List<com.taskflow.domain.model.Subtask>>() {});
            }
            
            List<com.taskflow.domain.model.Comment> comments = null;
            if (payload.containsKey("comments") && payload.get("comments") != null) {
                comments = mapper.convertValue(payload.get("comments"), new com.fasterxml.jackson.core.type.TypeReference<List<com.taskflow.domain.model.Comment>>() {});
            }
            
            List<com.taskflow.domain.model.Attachment> attachments = null;
            if (payload.containsKey("attachments") && payload.get("attachments") != null) {
                attachments = mapper.convertValue(payload.get("attachments"), new com.fasterxml.jackson.core.type.TypeReference<List<com.taskflow.domain.model.Attachment>>() {});
            }

            Task task = updateTaskUseCase.updateTask(taskId, title, description, assigneeIds, priority, dueDate, statusColumnId, assignedDate, startDate, subtasks, comments, attachments, userDetails.getId());
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getClass().getName() + ": " + e.getMessage()));
        }
    }

    // ─── GET /api/v1/tasks/{taskId} ───────────────────────────────────────────
    // Spec: docs/api-spec.yaml → GET /work-orders/{id} → 200 OK | 404
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<?> getTask(@PathVariable UUID taskId,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        try {
            Task task = getTaskUseCase.getTask(taskId, userDetails.getId());
            // Fix: trả DTO thay vì entity
            String columnName = resolveColumnName(task.getStatusColumnId());
            return ResponseEntity.ok(WorkOrderResponse.from(task, columnName));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.forbidden(e.getMessage(), requestUri));
        } catch (IllegalArgumentException e) {
            // Task not found -> 404
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.notFound(e.getMessage(), requestUri));
        }
    }

    // ─── GET /api/v1/work-orders ──────────────────────────────────────────────────
    // Spec: docs/api-spec.yaml → GET /work-orders (liệt kê danh sách task trong board)
    @GetMapping("/boards/{boardId}/tasks")
    public ResponseEntity<?> searchTasks(@PathVariable UUID boardId,
                                         @org.springframework.web.bind.annotation.RequestParam(required = false) Long statusColumnId,
                                         @org.springframework.web.bind.annotation.RequestParam(required = false) Long assigneeId,
                                         @org.springframework.web.bind.annotation.RequestParam(required = false) Priority priority,
                                         @org.springframework.web.bind.annotation.RequestParam(required = false) String search,
                                         @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean overdueOnly,
                                         @org.springframework.web.bind.annotation.RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) Instant assignedDateFrom,
                                         @org.springframework.web.bind.annotation.RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) Instant assignedDateTo,
                                         @org.springframework.web.bind.annotation.RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) Instant startDateFrom,
                                         @org.springframework.web.bind.annotation.RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) Instant startDateTo,
                                         @org.springframework.web.bind.annotation.RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) Instant endDateFrom,
                                         @org.springframework.web.bind.annotation.RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) Instant endDateTo,
                                         @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
                                         @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        try {
            if (size > 100) size = 100;
            if (search != null && search.length() > 100) {
                return ResponseEntity.badRequest()
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                        .body(ProblemDetail.badRequest("Query string cannot exceed 100 characters", requestUri));
            }

            com.taskflow.infrastructure.web.dto.PagedResponse<Task> paged = searchTasksUseCase.searchTasks(
                boardId, statusColumnId, assigneeId, priority, search, overdueOnly, 
                assignedDateFrom, assignedDateTo, startDateFrom, startDateTo, endDateFrom, endDateTo,
                userDetails.getId(), page, size);

            Map<Long, String> colMap = boardColumnRepositoryPort.findByBoardId(boardId)
                    .stream()
                    .collect(java.util.stream.Collectors.toMap(BoardColumn::getId, BoardColumn::getName));

            List<WorkOrderSummary> mappedItems = paged.content().stream()
                    .map(t -> WorkOrderSummary.from(t, colMap.getOrDefault(t.getStatusColumnId(), "UNKNOWN")))
                    .toList();

            return ResponseEntity.ok(PagedWorkOrderResponse.from(paged, mappedItems));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.forbidden(e.getMessage(), requestUri));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.badRequest(e.getMessage(), requestUri));
        }
    }

    @GetMapping("/boards/{boardId}/tasks/search")
    public ResponseEntity<?> searchTasksAdvanced(@PathVariable UUID boardId,
                                                 @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
                                                 @org.springframework.web.bind.annotation.RequestParam(required = false) String status,
                                                 @org.springframework.web.bind.annotation.RequestParam(required = false) Long assigneeId,
                                                 @org.springframework.web.bind.annotation.RequestParam(required = false) Priority priority,
                                                 @org.springframework.web.bind.annotation.RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) Instant assignedDateFrom,
                                                 @org.springframework.web.bind.annotation.RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) Instant assignedDateTo,
                                                 @org.springframework.web.bind.annotation.RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) Instant startDateFrom,
                                                 @org.springframework.web.bind.annotation.RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) Instant startDateTo,
                                                 @org.springframework.web.bind.annotation.RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) Instant endDateFrom,
                                                 @org.springframework.web.bind.annotation.RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) Instant endDateTo,
                                                 @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
                                                 @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                                 HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if (q != null && q.length() > 100) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.badRequest("Query string cannot exceed 100 characters", requestUri));
        }

        List<BoardColumn> columns = boardColumnRepositoryPort.findByBoardId(boardId);
        Long statusColumnId = null;
        if (status != null) {
            statusColumnId = columns.stream()
                    .filter(c -> c.getName().equalsIgnoreCase(status))
                    .map(BoardColumn::getId)
                    .findFirst()
                    .orElse(-1L);
        }

        Map<Long, String> colMap = columns.stream()
                .collect(java.util.stream.Collectors.toMap(BoardColumn::getId, BoardColumn::getName));

        try {
            if (size > 100) size = 100;
            com.taskflow.infrastructure.web.dto.PagedResponse<Task> paged = searchTasksUseCase.searchTasks(
                boardId, statusColumnId, assigneeId, priority, q, null, 
                assignedDateFrom, assignedDateTo, startDateFrom, startDateTo, endDateFrom, endDateTo,
                userDetails.getId(), page, size);
            
            List<WorkOrderSummary> mappedItems = paged.content().stream()
                    .map(t -> WorkOrderSummary.from(t, colMap.getOrDefault(t.getStatusColumnId(), "UNKNOWN")))
                    .toList();

            return ResponseEntity.ok(PagedWorkOrderResponse.from(paged, mappedItems));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.forbidden(e.getMessage(), requestUri));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.badRequest(e.getMessage(), requestUri));
        }
    }
    
    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable UUID taskId,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            deleteTaskUseCase.deleteTask(taskId, userDetails.getId());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── PATCH /api/v1/tasks/{taskId}/status ─────────────────────────────────
    // Spec: docs/api-spec.yaml (status update endpoint)
    @PatchMapping("/tasks/{taskId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID taskId,
                                          @RequestBody Map<String, Object> payload,
                                          @AuthenticationPrincipal CustomUserDetails userDetails,
                                          HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        try {
            Long statusColumnId = Long.valueOf(payload.get("statusColumnId").toString());
            String note = payload.containsKey("note") ? (String) payload.get("note") : null;

            Task task = updateTaskStatusUseCase.updateTaskStatus(taskId, statusColumnId, note, userDetails.getId());
            // Fix: trả DTO
            String columnName = resolveColumnName(task.getStatusColumnId());
            return ResponseEntity.ok(WorkOrderResponse.from(task, columnName));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.forbidden(e.getMessage(), requestUri));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.badRequest(e.getMessage(), requestUri));
        }
    }

    // ─── PATCH /api/v1/tasks/{taskId}/move ───────────────────────────────────
    @PatchMapping("/tasks/{taskId}/move")
    public ResponseEntity<?> moveTask(@PathVariable UUID taskId,
                                      @RequestBody Map<String, Object> payload,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        try {
            Long sourceColumnId = Long.valueOf(payload.get("sourceColumnId").toString());
            Long targetColumnId = Long.valueOf(payload.get("targetColumnId").toString());
            int sourceIndex = Integer.parseInt(payload.get("sourceIndex").toString());
            int targetIndex = Integer.parseInt(payload.get("targetIndex").toString());

            Task task = moveTaskUseCase.moveTask(
                    taskId, sourceColumnId, targetColumnId, sourceIndex, targetIndex,
                    userDetails.getId());
            // Fix: trả DTO
            String columnName = resolveColumnName(task.getStatusColumnId());
            return ResponseEntity.ok(WorkOrderResponse.from(task, columnName));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.forbidden(e.getMessage(), requestUri));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.badRequest(e.getMessage(), requestUri));
        }
    }

    // ─── PATCH /api/v1/tasks/{taskId}/block ──────────────────────────────────
    // Spec: domain-model.md → Invariant I-06: blockedReason bắt buộc
    // Fix: validation lỗi trả 400 ProblemDetail thay vì raw Map
    @PatchMapping("/tasks/{taskId}/block")
    public ResponseEntity<?> blockTask(@PathVariable UUID taskId,
                                       @RequestBody Map<String, Object> payload,
                                       @AuthenticationPrincipal CustomUserDetails userDetails,
                                       HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        try {
            String reason = payload.containsKey("reason") ? (String) payload.get("reason") : null;

            // Invariant I-06 check ở controller level (service cũng check — defense in depth)
            if (reason == null || reason.isBlank()) {
                return ResponseEntity.badRequest()
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                        .body(ProblemDetail.badRequest(
                                "blockedReason is required when blocking a Work Order (domain-model.md Invariant I-06)",
                                requestUri,
                                List.of(new ProblemDetail.FieldViolation("reason", "must not be blank"))
                        ));
            }

            Task task = blockTaskUseCase.blockTask(taskId, reason, userDetails.getId());
            // Fix: trả DTO
            String columnName = resolveColumnName(task.getStatusColumnId());
            return ResponseEntity.ok(WorkOrderResponse.from(task, columnName));

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.forbidden(e.getMessage(), requestUri));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.badRequest(e.getMessage(), requestUri));
        }
    }

    // ─── PATCH /api/v1/tasks/{taskId}/unblock ────────────────────────────────
    @PatchMapping("/tasks/{taskId}/unblock")
    public ResponseEntity<?> unblockTask(@PathVariable UUID taskId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        try {
            Task task = unblockTaskUseCase.unblockTask(taskId, userDetails.getId());
            // Fix: trả DTO
            String columnName = resolveColumnName(task.getStatusColumnId());
            return ResponseEntity.ok(WorkOrderResponse.from(task, columnName));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.forbidden(e.getMessage(), requestUri));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.badRequest(e.getMessage(), requestUri));
        }
    }

    // ─── Helper: resolve column name từ columnId ──────────────────────────────
    // Dùng để populate statusColumnName trong WorkOrderResponse DTO
    private String resolveColumnName(Long columnId) {
        if (columnId == null) return null;
        return boardColumnRepositoryPort.findById(columnId)
                .map(BoardColumn::getName)
                .orElse(null);
    }
}

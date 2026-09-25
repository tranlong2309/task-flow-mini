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
import com.taskflow.domain.model.Priority;
import com.taskflow.domain.model.Task;
import com.taskflow.infrastructure.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1")
public class TaskController {

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

    @PostMapping("/tasks")
    public ResponseEntity<?> createTask(@RequestBody Map<String, Object> payload,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
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
            Instant dueDate = payload.get("dueDate") != null ? Instant.parse(payload.get("dueDate").toString()) : null;
            Long statusColumnId = payload.get("statusColumnId") != null ? Long.valueOf(payload.get("statusColumnId").toString()) : null;

            Task task = createTaskUseCase.createTask(boardId, title, description, assigneeIds, priority, dueDate, statusColumnId, userDetails.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(task);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<?> getTask(@PathVariable UUID taskId,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Task task = getTaskUseCase.getTask(taskId, userDetails.getId());
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

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
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (size > 100) size = 100;
        com.taskflow.infrastructure.web.dto.PagedResponse<Task> paged = searchTasksUseCase.searchTasks(
            boardId, statusColumnId, assigneeId, priority, search, overdueOnly, 
            assignedDateFrom, assignedDateTo, startDateFrom, startDateTo, endDateFrom, endDateTo,
            userDetails.getId(), page, size);
        return ResponseEntity.ok(paged);
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
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        if (q != null && q.length() > 100) {
            return ResponseEntity.badRequest().body(Map.of("error", "Query string cannot exceed 100 characters"));
        }

        Long statusColumnId = null;
        List<com.taskflow.domain.model.BoardColumn> columns = boardColumnRepositoryPort.findByBoardId(boardId);
        
        if (status != null) {
            statusColumnId = columns.stream()
                    .filter(c -> c.getName().equalsIgnoreCase(status))
                    .map(com.taskflow.domain.model.BoardColumn::getId)
                    .findFirst()
                    .orElse(-1L); // Not found -> non-existent column to return empty
        }

        Map<Long, String> colMap = columns.stream().collect(java.util.stream.Collectors.toMap(com.taskflow.domain.model.BoardColumn::getId, com.taskflow.domain.model.BoardColumn::getName));

        try {
            if (size > 100) size = 100;
            com.taskflow.infrastructure.web.dto.PagedResponse<Task> paged = searchTasksUseCase.searchTasks(
                boardId, statusColumnId, assigneeId, priority, q, null, 
                assignedDateFrom, assignedDateTo, startDateFrom, startDateTo, endDateFrom, endDateTo,
                userDetails.getId(), page, size);
            
            List<com.taskflow.domain.model.TaskSearchResult> items = paged.content().stream().map(t -> {
                Long primaryAssignee = (t.getAssigneeIds() != null && !t.getAssigneeIds().isEmpty()) ? t.getAssigneeIds().iterator().next() : null;
                return new com.taskflow.domain.model.TaskSearchResult(
                    t.getId(), 
                    t.getTitle(), 
                    colMap.getOrDefault(t.getStatusColumnId(), "UNKNOWN"), 
                    primaryAssignee, 
                    t.getPriority(), 
                    t.getDueDate()
                );
            }).collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "items", items,
                "totalElements", paged.totalElements(),
                "totalPages", paged.totalPages(),
                "page", paged.page(),
                "size", paged.size()
            ));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(403).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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

    @PatchMapping("/tasks/{taskId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID taskId,
                                          @RequestBody Map<String, Object> payload,
                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long statusColumnId = Long.valueOf(payload.get("statusColumnId").toString());
            String note = payload.containsKey("note") ? (String) payload.get("note") : null;
            
            Task task = updateTaskStatusUseCase.updateTaskStatus(taskId, statusColumnId, note, userDetails.getId());
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/tasks/{taskId}/move")
    public ResponseEntity<?> moveTask(@PathVariable UUID taskId,
                                      @RequestBody Map<String, Object> payload,
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long sourceColumnId = Long.valueOf(payload.get("sourceColumnId").toString());
            Long targetColumnId = Long.valueOf(payload.get("targetColumnId").toString());
            int sourceIndex = Integer.parseInt(payload.get("sourceIndex").toString());
            int targetIndex = Integer.parseInt(payload.get("targetIndex").toString());
            
            Task task = moveTaskUseCase.moveTask(taskId, sourceColumnId, targetColumnId, sourceIndex, targetIndex, userDetails.getId());
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/tasks/{taskId}/block")
    public ResponseEntity<?> blockTask(@PathVariable UUID taskId,
                                       @RequestBody Map<String, Object> payload,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            String reason = payload.containsKey("reason") ? (String) payload.get("reason") : null;
            Task task = blockTaskUseCase.blockTask(taskId, reason, userDetails.getId());
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/tasks/{taskId}/unblock")
    public ResponseEntity<?> unblockTask(@PathVariable UUID taskId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Task task = unblockTaskUseCase.unblockTask(taskId, userDetails.getId());
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

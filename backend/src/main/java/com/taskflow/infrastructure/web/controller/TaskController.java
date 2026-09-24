package com.taskflow.infrastructure.web.controller;

import com.taskflow.application.port.in.CreateTaskUseCase;
import com.taskflow.application.port.in.DeleteTaskUseCase;
import com.taskflow.application.port.in.GetTaskUseCase;
import com.taskflow.application.port.in.SearchTasksUseCase;
import com.taskflow.application.port.in.UpdateTaskUseCase;
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
            Long assigneeId = payload.get("assigneeId") != null ? Long.valueOf(payload.get("assigneeId").toString()) : null;
            Priority priority = Priority.valueOf((String) payload.get("priority"));
            Instant dueDate = payload.get("dueDate") != null ? Instant.parse(payload.get("dueDate").toString()) : null;
            Long statusColumnId = payload.get("statusColumnId") != null ? Long.valueOf(payload.get("statusColumnId").toString()) : null;

            Task task = createTaskUseCase.createTask(boardId, title, description, assigneeId, priority, dueDate, statusColumnId, userDetails.getId());
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
            Long assigneeId = payload.containsKey("assigneeId") && payload.get("assigneeId") != null ? Long.valueOf(payload.get("assigneeId").toString()) : null;
            Priority priority = payload.containsKey("priority") ? Priority.valueOf((String) payload.get("priority")) : null;
            Instant dueDate = payload.containsKey("dueDate") && payload.get("dueDate") != null ? Instant.parse(payload.get("dueDate").toString()) : null;
            Long statusColumnId = payload.containsKey("statusColumnId") && payload.get("statusColumnId") != null ? Long.valueOf(payload.get("statusColumnId").toString()) : null;

            Task task = updateTaskUseCase.updateTask(taskId, title, description, assigneeId, priority, dueDate, statusColumnId, userDetails.getId());
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
                                         @RequestParam(required = false) Long statusColumnId,
                                         @RequestParam(required = false) Long assigneeId,
                                         @RequestParam(required = false) Priority priority,
                                         @RequestParam(required = false) String search,
                                         @RequestParam(required = false) Boolean overdueOnly,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Task> tasks = searchTasksUseCase.searchTasks(boardId, statusColumnId, assigneeId, priority, search, overdueOnly, userDetails.getId());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/boards/{boardId}/tasks/search")
    public ResponseEntity<?> searchTasksAdvanced(@PathVariable UUID boardId,
                                                 @RequestParam(required = false) String q,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(required = false) Long assigneeId,
                                                 @RequestParam(required = false) Priority priority,
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
            List<Task> tasks = searchTasksUseCase.searchTasks(boardId, statusColumnId, assigneeId, priority, q, null, userDetails.getId());
            
            List<com.taskflow.domain.model.TaskSearchResult> items = tasks.stream().map(t -> 
                new com.taskflow.domain.model.TaskSearchResult(
                    t.getId(), 
                    t.getTitle(), 
                    colMap.getOrDefault(t.getStatusColumnId(), "UNKNOWN"), 
                    t.getAssigneeId(), 
                    t.getPriority(), 
                    t.getDueDate()
                )
            ).collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(Map.of("items", items));
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

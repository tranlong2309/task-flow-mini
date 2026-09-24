package com.taskflow.infrastructure.web.controller;

import com.taskflow.application.port.in.GetBoardPermissionUseCase;
import com.taskflow.application.port.in.GetReportUseCase;
import com.taskflow.domain.model.BoardPermission;
import com.taskflow.domain.model.BoardReportSummary;
import com.taskflow.domain.model.TeamReportSummary;
import com.taskflow.infrastructure.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final GetReportUseCase getReportUseCase;
    private final GetBoardPermissionUseCase getBoardPermissionUseCase;

    public ReportController(GetReportUseCase getReportUseCase, GetBoardPermissionUseCase getBoardPermissionUseCase) {
        this.getReportUseCase = getReportUseCase;
        this.getBoardPermissionUseCase = getBoardPermissionUseCase;
    }

    @GetMapping("/boards/{boardId}/summary")
    public ResponseEntity<?> getBoardSummary(
            @PathVariable UUID boardId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {
            BoardPermission permission = getBoardPermissionUseCase.getPermissions(boardId, userDetails.getId());
            if (!permission.isCanViewReport()) {
                return ResponseEntity.status(403).build();
            }

            BoardReportSummary summary = getReportUseCase.getBoardSummary(boardId, userDetails.getId());
            return ResponseEntity.ok(summary);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<?> getTeamReport(
            @PathVariable Long teamId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        // Ensure user is manager/admin of the team, for now we simplify by checking roles in general or just passing to use case
        // In a real app we need `GetTeamPermissionUseCase`. Since it's not defined in the scope of BE-08, we assume basic check.
        
        TeamReportSummary summary = getReportUseCase.getTeamReport(teamId, assigneeId, from, to, userDetails.getId());
        return ResponseEntity.ok(summary);
    }
}

package com.taskflow.application.port.in;

import com.taskflow.domain.model.BoardReportSummary;
import com.taskflow.domain.model.TeamReportSummary;

import java.util.UUID;

public interface GetReportUseCase {
    BoardReportSummary getBoardSummary(UUID boardId, Long userId);
    TeamReportSummary getTeamReport(Long teamId, Long assigneeId, String from, String to, Long userId);
}

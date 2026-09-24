package com.taskflow.application.service;

import com.taskflow.application.port.in.GetReportUseCase;
import com.taskflow.domain.model.*;
import com.taskflow.domain.repository.BoardColumnRepositoryPort;
import com.taskflow.domain.repository.BoardRepositoryPort;
import com.taskflow.domain.repository.TaskRepositoryPort;
import com.taskflow.domain.repository.UserRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportApplicationService implements GetReportUseCase {

    private final TaskRepositoryPort taskRepositoryPort;
    private final BoardColumnRepositoryPort boardColumnRepositoryPort;
    private final BoardRepositoryPort boardRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public ReportApplicationService(TaskRepositoryPort taskRepositoryPort,
                                    BoardColumnRepositoryPort boardColumnRepositoryPort,
                                    BoardRepositoryPort boardRepositoryPort,
                                    UserRepositoryPort userRepositoryPort) {
        this.taskRepositoryPort = taskRepositoryPort;
        this.boardColumnRepositoryPort = boardColumnRepositoryPort;
        this.boardRepositoryPort = boardRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public BoardReportSummary getBoardSummary(UUID boardId, Long userId) {
        // Assume permissions checked at controller level or using another service (can reuse GetBoardPermissionUseCase but it's fine for now, we'll check it in controller)
        
        long totalTasks = taskRepositoryPort.countByBoardId(boardId);
        long done = taskRepositoryPort.countByBoardIdAndCompletedAtIsNotNull(boardId);
        long inProgress = totalTasks - done;
        long blocked = taskRepositoryPort.countByBoardIdAndIsBlockedTrue(boardId);
        long overdue = taskRepositoryPort.countByBoardIdAndDueDateBeforeAndCompletedAtIsNull(boardId, Instant.now());


        double completionRate = calculateCompletionRate(done, totalTasks);

        return new BoardReportSummary(boardId, Instant.now(), totalTasks, done, inProgress, blocked, overdue, completionRate);
    }

    @Override
    public TeamReportSummary getTeamReport(Long teamId, Long assigneeId, String from, String to, Long userId) {
        List<Board> boards = boardRepositoryPort.findByTeamId(teamId);
        
        java.util.List<UUID> boardIds = boards.stream().map(Board::getId).collect(Collectors.toList());
        Instant fromInstant = from != null ? Instant.parse(from + "T00:00:00Z") : null;
        Instant toInstant = to != null ? Instant.parse(to + "T23:59:59Z") : null;
        List<Task> allTasks = boardIds.isEmpty() ? new java.util.ArrayList<>() : taskRepositoryPort.searchTasksByBoardIds(boardIds, assigneeId, fromInstant, toInstant);

        long totalTasks = allTasks.size();
        long done = 0;
        long blocked = 0;
        long overdue = 0;

        Instant now = Instant.now();

        for (Task task : allTasks) {
            boolean isDone = task.getCompletedAt() != null;
            if (isDone) {
                done++;
            }
            if (Boolean.TRUE.equals(task.getIsBlocked())) {
                blocked++;
            }
            if (!isDone && task.getDueDate() != null && task.getDueDate().isBefore(now)) {
                overdue++;
            }
        }

        double completionRate = calculateCompletionRate(done, totalTasks);

        return new TeamReportSummary(teamId, from, to, totalTasks, done, blocked, overdue, completionRate, allTasks);
    }

    protected double calculateCompletionRate(long done, long total) {
        if (total == 0) return 0.0;
        double rate = (double) done / total * 100;
        return Math.round(rate * 10.0) / 10.0;
    }
}

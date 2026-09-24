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
        List<Task> tasks = taskRepositoryPort.searchTasks(boardId, null, null, null, null, null);

        Map<Long, String> columnNames = boardColumnRepositoryPort.findByBoardId(boardId).stream()
                .collect(Collectors.toMap(BoardColumn::getId, BoardColumn::getName));

        long totalTasks = tasks.size();
        long done = 0;
        long inProgress = 0;
        long blocked = 0;
        long overdue = 0;

        Instant now = Instant.now();

        for (Task task : tasks) {
            boolean isDone = task.getCompletedAt() != null;
            if (isDone) {
                done++;
            } else {
                inProgress++;
            }

            if (Boolean.TRUE.equals(task.getIsBlocked())) {
                blocked++;
            }

            if (!isDone && task.getDueDate() != null && task.getDueDate().isBefore(now)) {
                overdue++;
            }
        }

        double completionRate = calculateCompletionRate(done, totalTasks);

        return new BoardReportSummary(boardId, Instant.now(), totalTasks, done, inProgress, blocked, overdue, completionRate);
    }

    @Override
    public TeamReportSummary getTeamReport(Long teamId, Long assigneeId, String from, String to, Long userId) {
        List<Board> boards = boardRepositoryPort.findByTeamId(teamId);
        
        List<Task> allTasks = new ArrayList<>();
        for (Board board : boards) {
            // Note: date range filter (from/to) not strictly pushed to DB here for simplicity, but in a real app we'd use Specifications
            List<Task> boardTasks = taskRepositoryPort.searchTasks(board.getId(), null, assigneeId, null, null, null);
            allTasks.addAll(boardTasks);
        }

        // Apply date range filter in memory
        Instant fromInstant = from != null ? Instant.parse(from + "T00:00:00Z") : null;
        Instant toInstant = to != null ? Instant.parse(to + "T23:59:59Z") : null;

        if (fromInstant != null || toInstant != null) {
            allTasks = allTasks.stream().filter(t -> {
                Instant date = t.getCreatedAt();
                if (fromInstant != null && date.isBefore(fromInstant)) return false;
                if (toInstant != null && date.isAfter(toInstant)) return false;
                return true;
            }).collect(Collectors.toList());
        }

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

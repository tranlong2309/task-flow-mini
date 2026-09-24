package com.taskflow.domain.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BoardWorkloadSummary {
    private UUID boardId;
    private Map<String, Long> summary;
    private List<AssigneeWorkload> byAssignee;

    public BoardWorkloadSummary(UUID boardId, Map<String, Long> summary, List<AssigneeWorkload> byAssignee) {
        this.boardId = boardId;
        this.summary = summary;
        this.byAssignee = byAssignee;
    }

    public UUID getBoardId() { return boardId; }
    public Map<String, Long> getSummary() { return summary; }
    public List<AssigneeWorkload> getByAssignee() { return byAssignee; }
}

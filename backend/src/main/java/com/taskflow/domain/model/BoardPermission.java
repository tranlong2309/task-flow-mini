package com.taskflow.domain.model;

public class BoardPermission {
    private boolean canCreateTask;
    private boolean canEditTask;
    private boolean canDeleteBoard;
    private boolean canViewReport;

    public BoardPermission(boolean canCreateTask, boolean canEditTask, boolean canDeleteBoard, boolean canViewReport) {
        this.canCreateTask = canCreateTask;
        this.canEditTask = canEditTask;
        this.canDeleteBoard = canDeleteBoard;
        this.canViewReport = canViewReport;
    }

    public boolean isCanCreateTask() { return canCreateTask; }
    public boolean isCanEditTask() { return canEditTask; }
    public boolean isCanDeleteBoard() { return canDeleteBoard; }
    public boolean isCanViewReport() { return canViewReport; }
}

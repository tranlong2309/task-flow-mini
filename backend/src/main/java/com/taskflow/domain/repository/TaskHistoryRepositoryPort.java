package com.taskflow.domain.repository;

import com.taskflow.domain.model.TaskHistory;

public interface TaskHistoryRepositoryPort {
    void save(TaskHistory taskHistory);
}

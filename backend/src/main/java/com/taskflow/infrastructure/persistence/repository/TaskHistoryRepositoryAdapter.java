package com.taskflow.infrastructure.persistence.repository;

import com.taskflow.domain.model.TaskHistory;
import com.taskflow.domain.repository.TaskHistoryRepositoryPort;
import com.taskflow.infrastructure.persistence.entity.TaskHistoryEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskHistoryRepositoryAdapter implements TaskHistoryRepositoryPort {

    private final SpringDataTaskHistoryRepository springDataTaskHistoryRepository;

    public TaskHistoryRepositoryAdapter(SpringDataTaskHistoryRepository springDataTaskHistoryRepository) {
        this.springDataTaskHistoryRepository = springDataTaskHistoryRepository;
    }

    @Override
    public void save(TaskHistory taskHistory) {
        TaskHistoryEntity entity = new TaskHistoryEntity();
        entity.setTaskId(taskHistory.getTaskId());
        entity.setFieldName(taskHistory.getFieldName());
        entity.setOldValue(taskHistory.getOldValue());
        entity.setNewValue(taskHistory.getNewValue());
        entity.setChangedBy(taskHistory.getChangedBy());
        entity.setChangedAt(taskHistory.getChangedAt());
        springDataTaskHistoryRepository.save(entity);
    }
}

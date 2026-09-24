package com.taskflow.infrastructure.persistence.repository;

import com.taskflow.domain.model.BoardColumn;
import com.taskflow.domain.repository.BoardColumnRepositoryPort;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class BoardColumnRepositoryAdapter implements BoardColumnRepositoryPort {
    private final SpringDataBoardColumnRepository repository;

    public BoardColumnRepositoryAdapter(SpringDataBoardColumnRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<BoardColumn> findById(Long id) {
        return repository.findById(id).map(e -> new BoardColumn(e.getId(), e.getBoardId(), e.getName(), e.getPosition()));
    }
}

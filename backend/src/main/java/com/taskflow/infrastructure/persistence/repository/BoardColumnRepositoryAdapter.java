package com.taskflow.infrastructure.persistence.repository;

import java.util.List;

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
        return repository.findById(id).map(e -> new BoardColumn(e.getId(), e.getBoardId(), e.getName(), e.getPosition(), e.getTone()));
    }

    @Override
    public List<BoardColumn> findByBoardId(java.util.UUID boardId) {
        return repository.findByBoardIdOrderByPositionAsc(boardId).stream()
                .map(e -> new BoardColumn(e.getId(), e.getBoardId(), e.getName(), e.getPosition(), e.getTone()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public List<BoardColumn> saveAll(List<BoardColumn> columns) {
        List<com.taskflow.infrastructure.persistence.entity.BoardColumnEntity> entities = columns.stream()
                .map(c -> {
                    com.taskflow.infrastructure.persistence.entity.BoardColumnEntity e = new com.taskflow.infrastructure.persistence.entity.BoardColumnEntity();
                    e.setId(c.getId());
                    e.setBoardId(c.getBoardId());
                    e.setName(c.getName());
                    e.setPosition(c.getPosition());
                    e.setTone(c.getTone());
                    return e;
                }).collect(java.util.stream.Collectors.toList());
        
        return repository.saveAll(entities).stream()
                .map(e -> new BoardColumn(e.getId(), e.getBoardId(), e.getName(), e.getPosition(), e.getTone()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteAllByBoardId(java.util.UUID boardId) {
        repository.deleteByBoardId(boardId);
    }
}

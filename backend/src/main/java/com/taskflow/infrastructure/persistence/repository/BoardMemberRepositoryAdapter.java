package com.taskflow.infrastructure.persistence.repository;

import com.taskflow.domain.repository.BoardMemberRepositoryPort;
import com.taskflow.infrastructure.persistence.entity.BoardMemberEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class BoardMemberRepositoryAdapter implements BoardMemberRepositoryPort {

    private final SpringDataBoardMemberRepository springDataBoardMemberRepository;

    public BoardMemberRepositoryAdapter(SpringDataBoardMemberRepository springDataBoardMemberRepository) {
        this.springDataBoardMemberRepository = springDataBoardMemberRepository;
    }

    @Override
    public Optional<String> getRoleInBoard(UUID boardId, Long userId) {
        return springDataBoardMemberRepository.findByBoardIdAndUserId(boardId, userId)
                .map(BoardMemberEntity::getRoleName);
    }
}

package com.taskflow.application.service;

import com.taskflow.domain.model.Board;
import com.taskflow.domain.repository.BoardRepositoryPort;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BoardApplicationServiceTest {

    @Test
    void createBoard_shouldCreateAndPersistBoard() {
        BoardRepositoryPort repository = mock(BoardRepositoryPort.class);
        com.taskflow.domain.repository.BoardColumnRepositoryPort columnRepository = mock(com.taskflow.domain.repository.BoardColumnRepositoryPort.class);
        com.taskflow.domain.repository.BoardMemberRepositoryPort memberRepository = mock(com.taskflow.domain.repository.BoardMemberRepositoryPort.class);
        BoardApplicationService service = new BoardApplicationService(repository, columnRepository, memberRepository);

        Board sample = new Board(UUID.randomUUID(), "Board A", "Desc", 1L);
        when(repository.save(any(Board.class))).thenReturn(sample);

        Board result = service.createBoard("Board A", "Desc", 1L, 1L);

        assertNotNull(result);
        assertEquals("Board A", result.getName());
        assertEquals(1L, result.getTeamId());
        verify(repository, times(1)).save(any(Board.class));
    }

    @Test
    void createBoard_shouldHandleMissingName() {
        BoardRepositoryPort repository = mock(BoardRepositoryPort.class);
        com.taskflow.domain.repository.BoardColumnRepositoryPort columnRepository = mock(com.taskflow.domain.repository.BoardColumnRepositoryPort.class);
        com.taskflow.domain.repository.BoardMemberRepositoryPort memberRepository = mock(com.taskflow.domain.repository.BoardMemberRepositoryPort.class);
        BoardApplicationService service = new BoardApplicationService(repository, columnRepository, memberRepository);

        assertThrows(IllegalArgumentException.class, () -> {
            service.createBoard(null, "Desc", 1L, 1L);
        });
    }
}

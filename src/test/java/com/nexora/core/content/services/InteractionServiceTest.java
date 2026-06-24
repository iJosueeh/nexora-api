package com.nexora.core.content.services;

import com.nexora.core.application.content.usecases.interaction.commands.TogglePostLikeUseCase;
import com.nexora.core.domain.content.repositories.LikeRepository;
import com.nexora.core.application.security.services.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InteractionServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private TogglePostLikeUseCase togglePostLikeUseCase;

    private UUID userId;
    private UUID postId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        postId = UUID.randomUUID();
    }

    @Test
    void toggleLikeShouldDeleteWhenAlreadyLiked() {
        when(securityService.getCurrentUserId()).thenReturn(userId);
        when(likeRepository.existsPostLike(postId, userId)).thenReturn(true);

        boolean result = togglePostLikeUseCase.execute(postId);

        assertFalse(result);
        verify(likeRepository).removePostLike(postId, userId);
        verify(likeRepository, never()).addPostLike(any(), any());
    }

    @Test
    void toggleLikeShouldInsertWhenNotLiked() {
        when(securityService.getCurrentUserId()).thenReturn(userId);
        when(likeRepository.existsPostLike(postId, userId)).thenReturn(false);

        boolean result = togglePostLikeUseCase.execute(postId);

        assertTrue(result);
        verify(likeRepository).addPostLike(postId, userId);
        verify(likeRepository, never()).removePostLike(any(), any());
    }
}

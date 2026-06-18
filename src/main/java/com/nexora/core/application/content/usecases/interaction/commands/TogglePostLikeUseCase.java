package com.nexora.core.application.content.usecases.interaction.commands;

import com.nexora.core.domain.content.repositories.LikeRepository;
import com.nexora.core.application.security.services.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TogglePostLikeUseCase {

    private final LikeRepository likeRepository;
    private final SecurityService securityService;

    public boolean execute(UUID postId) {
        UUID currentUserId = securityService.getCurrentUserId();

        if (likeRepository.existsPostLike(postId, currentUserId)) {
            likeRepository.removePostLike(postId, currentUserId);
            return false;
        } else {
            likeRepository.addPostLike(postId, currentUserId);
            return true;
        }
    }
}

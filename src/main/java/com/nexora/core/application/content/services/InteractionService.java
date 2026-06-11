package com.nexora.core.application.content.services;

import com.nexora.core.domain.content.repositories.LikeRepository;
import com.nexora.core.application.security.services.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final LikeRepository likeRepository;
    private final SecurityService securityService;

    @Transactional
    public boolean toggleLike(UUID postId) {
        UUID currentUserId = securityService.getCurrentUserId();

        if (likeRepository.existsPostLike(postId, currentUserId)) {
            likeRepository.removePostLike(postId, currentUserId);
            return false;
        } else {
            likeRepository.addPostLike(postId, currentUserId);
            return true;
        }
    }

    @Transactional
    public boolean toggleCommentLike(UUID commentId) {
        UUID currentUserId = securityService.getCurrentUserId();

        if (likeRepository.existsCommentLike(commentId, currentUserId)) {
            likeRepository.removeCommentLike(commentId, currentUserId);
            return false;
        } else {
            likeRepository.addCommentLike(commentId, currentUserId);
            return true;
        }
    }
}

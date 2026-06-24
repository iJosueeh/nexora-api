package com.nexora.core.domain.content.repositories;

import java.util.UUID;

public interface LikeRepository {
    boolean existsPostLike(UUID postId, UUID userId);
    void addPostLike(UUID postId, UUID userId);
    void removePostLike(UUID postId, UUID userId);
    boolean existsCommentLike(UUID commentId, UUID userId);
    void addCommentLike(UUID commentId, UUID userId);
    void removeCommentLike(UUID commentId, UUID userId);
}

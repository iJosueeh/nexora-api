package com.nexora.core.domain.content.repositories;

import com.nexora.core.domain.content.aggregates.Bookmark;
import java.util.List;
import java.util.UUID;

public interface BookmarkRepository {
    boolean existsByUserIdAndPostId(UUID userId, UUID postId);
    Bookmark save(Bookmark bookmark);
    void deleteByUserIdAndPostId(UUID userId, UUID postId);
    List<UUID> findPostIdsByUserId(UUID userId, int limit, int offset);
    long countByUserId(UUID userId);
}

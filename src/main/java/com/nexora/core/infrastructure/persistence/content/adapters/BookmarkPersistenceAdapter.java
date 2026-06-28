package com.nexora.core.infrastructure.persistence.content.adapters;

import com.nexora.core.domain.content.aggregates.Bookmark;
import com.nexora.core.domain.content.repositories.BookmarkRepository;
import com.nexora.core.infrastructure.persistence.content.entities.BookmarkJpaEntity;
import com.nexora.core.infrastructure.persistence.content.repositories.BookmarkJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookmarkPersistenceAdapter implements BookmarkRepository {

    private final BookmarkJpaRepository jpaRepository;

    @Override
    public boolean existsByUserIdAndPostId(UUID userId, UUID postId) {
        return jpaRepository.existsByUserIdAndPostId(userId, postId);
    }

    @Override
    public Bookmark save(Bookmark bookmark) {
        BookmarkJpaEntity entity = BookmarkJpaEntity.builder()
                .userId(bookmark.getUserId())
                .postId(bookmark.getPostId())
                .build();
        if (bookmark.getId() != null) {
            entity.setId(bookmark.getId());
        }
        BookmarkJpaEntity saved = jpaRepository.save(entity);
        bookmark.setId(saved.getId());
        return bookmark;
    }

    @Override
    public void deleteByUserIdAndPostId(UUID userId, UUID postId) {
        jpaRepository.deleteByUserIdAndPostId(userId, postId);
    }

    @Override
    public List<UUID> findPostIdsByUserId(UUID userId, int limit, int offset) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(offset / limit, limit))
                .stream()
                .map(BookmarkJpaEntity::getPostId)
                .toList();
    }

    @Override
    public long countByUserId(UUID userId) {
        return jpaRepository.countByUserId(userId);
    }
}

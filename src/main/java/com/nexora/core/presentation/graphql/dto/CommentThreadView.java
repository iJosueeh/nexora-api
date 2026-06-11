package com.nexora.core.presentation.graphql.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

public record CommentThreadView(
    UUID id,
    UUID postId,
    UUID parentId,
    FeedAuthorView autor,
    String contenido,
    OffsetDateTime createdAt,
    int likesCount,
    boolean isLiked,
    List<CommentThreadView> respuestas
) {
    public CommentThreadView(UUID id, UUID postId, UUID parentId, FeedAuthorView autor, String contenido, OffsetDateTime createdAt) {
        this(id, postId, parentId, autor, contenido, createdAt, 0, false, new ArrayList<>());
    }

    public CommentThreadView(UUID id, UUID postId, UUID parentId, FeedAuthorView autor, String contenido, OffsetDateTime createdAt, int likesCount, boolean isLiked) {
        this(id, postId, parentId, autor, contenido, createdAt, likesCount, isLiked, new ArrayList<>());
    }
}

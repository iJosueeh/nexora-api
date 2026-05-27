package com.nexora.core.graphql.dto;

import java.util.UUID;

public record CreateCommentInput(
    UUID postId,
    UUID parentId,
    String contenido
) {}

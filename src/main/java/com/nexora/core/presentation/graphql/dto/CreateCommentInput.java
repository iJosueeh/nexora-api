package com.nexora.core.presentation.graphql.dto;

import java.util.UUID;

public record CreateCommentInput(
    UUID postId,
    UUID parentId,
    String contenido
) {}

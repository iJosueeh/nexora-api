package com.nexora.core.presentation.graphql.dto;

import java.util.UUID;

public record FeedAuthorView(
        UUID id,
        String username,
        String fullName,
        String avatarUrl) {
}

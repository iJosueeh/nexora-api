package com.nexora.core.application.content.dto;

import java.util.UUID;

public record FeedAuthorView(
        UUID id,
        String username,
        String fullName,
        String avatarUrl) {
}

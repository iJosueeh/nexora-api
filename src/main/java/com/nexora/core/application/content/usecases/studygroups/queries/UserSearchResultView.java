package com.nexora.core.application.content.usecases.studygroups.queries;

import java.util.UUID;

public record UserSearchResultView(
    UUID userId,
    String username,
    String fullName,
    String avatarUrl
) {}

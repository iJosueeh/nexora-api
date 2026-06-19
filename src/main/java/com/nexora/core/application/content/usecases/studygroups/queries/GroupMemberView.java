package com.nexora.core.application.content.usecases.studygroups.queries;

import java.util.UUID;

public record GroupMemberView(
    UUID userId,
    String username,
    String fullName,
    String avatarUrl,
    String role,
    String status
) {}

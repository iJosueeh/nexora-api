package com.nexora.core.application.content.usecases.studygroups.queries;

import java.util.UUID;

public record PendingMemberView(
    UUID membershipId,
    UUID userId,
    String username,
    String fullName,
    String avatarUrl,
    String status,
    String role
) {}

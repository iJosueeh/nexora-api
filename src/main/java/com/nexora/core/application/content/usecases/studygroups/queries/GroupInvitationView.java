package com.nexora.core.application.content.usecases.studygroups.queries;

import java.util.UUID;

public record GroupInvitationView(
    UUID invitationId,
    UUID groupId,
    String groupName,
    String groupSlug,
    String inviterUsername,
    String inviterFullName,
    String inviterAvatarUrl,
    String invitedUsername,
    String invitedFullName,
    String invitedAvatarUrl,
    String status,
    UUID invitedUserId
) {}

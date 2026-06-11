package com.nexora.core.presentation.graphql.notification.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.nexora.core.presentation.graphql.dto.FeedAuthorView;
import com.nexora.core.presentation.graphql.dto.FeedPostView;
public record NotificationView(
    UUID id,
    String type,
    String content,
    Boolean isRead,
    OffsetDateTime createdAt,
    FeedAuthorView sender,
    FeedPostView post,
    Object event
) {}

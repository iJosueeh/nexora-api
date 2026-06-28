package com.nexora.core.application.notification.ports;

import com.nexora.core.presentation.graphql.notification.dto.NotificationView;
import java.util.List;
import java.util.UUID;

public interface NotificationViewRepository {
    List<NotificationView> findNotificationHistory(UUID userId, int limit, int offset);
    long countUnread(UUID userId);
}

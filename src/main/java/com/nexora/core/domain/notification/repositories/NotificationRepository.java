package com.nexora.core.domain.notification.repositories;

import com.nexora.core.domain.notification.aggregates.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, int limit, int offset);
    long countByUserIdAndIsReadFalse(UUID userId);
    Optional<Notification> findById(UUID id);
    Notification save(Notification notification);
    void markAsRead(UUID id);
    void markAllAsRead(UUID userId);
}

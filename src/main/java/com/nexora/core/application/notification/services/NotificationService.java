package com.nexora.core.application.notification.services;

import com.nexora.core.common.exception.ResourceNotFoundException;
import com.nexora.core.domain.notification.aggregates.Notification;
import com.nexora.core.domain.notification.repositories.NotificationRepository;
import com.nexora.core.application.security.services.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SecurityService securityService;

    @Transactional(readOnly = true)
    public List<Notification> getNotificationHistory(int limit, int offset) {
        UUID currentUserId = securityService.getCurrentUserId();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUserId, limit, offset);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        UUID currentUserId = securityService.getCurrentUserId();
        return notificationRepository.countByUserIdAndIsReadFalse(currentUserId);
    }

    @Transactional
    public boolean markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getUserId().equals(securityService.getCurrentUserId())) {
            throw new RuntimeException("Unauthorized");
        }
        notification.markAsRead();
        notificationRepository.save(notification);
        return true;
    }

    @Transactional
    public boolean markAllAsRead() {
        UUID currentUserId = securityService.getCurrentUserId();
        notificationRepository.markAllAsRead(currentUserId);
        return true;
    }
}

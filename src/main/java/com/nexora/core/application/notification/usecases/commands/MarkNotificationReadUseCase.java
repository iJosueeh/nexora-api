package com.nexora.core.application.notification.usecases.commands;

import com.nexora.core.common.exception.ResourceNotFoundException;
import com.nexora.core.domain.notification.aggregates.Notification;
import com.nexora.core.domain.notification.repositories.NotificationRepository;
import com.nexora.core.application.security.services.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MarkNotificationReadUseCase {

    private final NotificationRepository notificationRepository;
    private final SecurityService securityService;

    public boolean execute(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getUserId().equals(securityService.getCurrentUserId())) {
            throw new AccessDeniedException("Notificación no pertenece al usuario");
        }
        notification.markAsRead();
        notificationRepository.save(notification);
        return true;
    }
}

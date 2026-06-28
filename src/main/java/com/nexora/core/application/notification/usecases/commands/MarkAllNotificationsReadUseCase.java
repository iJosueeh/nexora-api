package com.nexora.core.application.notification.usecases.commands;

import com.nexora.core.domain.notification.repositories.NotificationRepository;
import com.nexora.core.application.security.services.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MarkAllNotificationsReadUseCase {

    private final NotificationRepository notificationRepository;
    private final SecurityService securityService;

    public boolean execute() {
        UUID currentUserId = securityService.getCurrentUserId();
        notificationRepository.markAllAsRead(currentUserId);
        return true;
    }
}

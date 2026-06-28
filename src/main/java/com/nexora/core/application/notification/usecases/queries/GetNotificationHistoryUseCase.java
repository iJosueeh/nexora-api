package com.nexora.core.application.notification.usecases.queries;

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
@Transactional(readOnly = true)
public class GetNotificationHistoryUseCase {

    private final NotificationRepository notificationRepository;
    private final SecurityService securityService;

    public List<Notification> execute(int limit, int offset) {
        UUID currentUserId = securityService.getCurrentUserId();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUserId, limit, offset);
    }
}

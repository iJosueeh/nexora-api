package com.nexora.core.application.notification.usecases.queries;

import com.nexora.core.domain.notification.repositories.NotificationRepository;
import com.nexora.core.application.security.services.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUnreadCountUseCase {

    private final NotificationRepository notificationRepository;
    private final SecurityService securityService;

    public long execute() {
        UUID currentUserId = securityService.getCurrentUserId();
        return notificationRepository.countByUserIdAndIsReadFalse(currentUserId);
    }
}

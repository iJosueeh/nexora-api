package com.nexora.core.presentation.graphql.notification;

import com.nexora.core.application.notification.ports.NotificationViewRepository;
import com.nexora.core.application.notification.usecases.commands.MarkAllNotificationsReadUseCase;
import com.nexora.core.application.notification.usecases.commands.MarkNotificationReadUseCase;
import com.nexora.core.application.security.services.SecurityService;
import com.nexora.core.presentation.graphql.notification.dto.NotificationView;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationViewRepository notificationViewRepository;
    private final SecurityService securityService;
    private final MarkNotificationReadUseCase markNotificationReadUseCase;
    private final MarkAllNotificationsReadUseCase markAllNotificationsReadUseCase;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<NotificationView> notificationHistory(@Argument Integer limit, @Argument Integer offset) {
        int safeLimit = limit == null ? 20 : Math.max(1, Math.min(limit, 100));
        int safeOffset = offset == null ? 0 : Math.max(0, offset);
        UUID currentUserId = securityService.getCurrentUserId();
        return notificationViewRepository.findNotificationHistory(currentUserId, safeLimit, safeOffset);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public int unreadNotificationsCount() {
        UUID currentUserId = securityService.getCurrentUserId();
        return (int) notificationViewRepository.countUnread(currentUserId);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public boolean markNotificationAsRead(@Argument UUID notificationId) {
        return markNotificationReadUseCase.execute(notificationId);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public boolean markAllNotificationsAsRead() {
        return markAllNotificationsReadUseCase.execute();
    }
}

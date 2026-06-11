package com.nexora.core.notification.services;

import com.nexora.core.application.notification.services.NotificationService;
import com.nexora.core.domain.notification.aggregates.Notification;
import com.nexora.core.domain.notification.repositories.NotificationRepository;
import com.nexora.core.domain.notification.valueobjects.NotificationType;
import com.nexora.core.application.security.services.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private NotificationService notificationService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void getUnreadCountShouldReturnCount() {
        when(securityService.getCurrentUserId()).thenReturn(userId);
        when(notificationRepository.countByUserIdAndIsReadFalse(userId)).thenReturn(5L);

        long count = notificationService.getUnreadCount();

        assertEquals(5L, count);
        verify(notificationRepository).countByUserIdAndIsReadFalse(userId);
    }

    @Test
    void markAsReadShouldSuccessWhenOwner() {
        UUID notifId = UUID.randomUUID();
        Notification notif = Notification.create(userId, UUID.randomUUID(), NotificationType.LIKE, "content", null, null);
        notif.setId(notifId);

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notif));
        when(securityService.getCurrentUserId()).thenReturn(userId);

        boolean result = notificationService.markAsRead(notifId);

        assertTrue(result);
        assertTrue(notif.isRead());
        verify(notificationRepository).save(notif);
    }

    @Test
    void markAsReadShouldThrowExceptionWhenNotOwner() {
        UUID notifId = UUID.randomUUID();
        Notification notif = Notification.create(UUID.randomUUID(), UUID.randomUUID(), NotificationType.LIKE, "content", null, null);
        notif.setId(notifId);

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notif));
        when(securityService.getCurrentUserId()).thenReturn(userId);

        assertThrows(RuntimeException.class, () -> notificationService.markAsRead(notifId));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAllAsReadShouldInvokeRepository() {
        when(securityService.getCurrentUserId()).thenReturn(userId);

        boolean result = notificationService.markAllAsRead();

        assertTrue(result);
        verify(notificationRepository).markAllAsRead(userId);
    }
}

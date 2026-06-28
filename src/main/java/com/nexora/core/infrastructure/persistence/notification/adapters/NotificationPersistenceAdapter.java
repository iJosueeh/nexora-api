package com.nexora.core.infrastructure.persistence.notification.adapters;

import com.nexora.core.domain.notification.aggregates.Notification;
import com.nexora.core.domain.notification.repositories.NotificationRepository;
import com.nexora.core.infrastructure.persistence.notification.entities.NotificationJpaEntity;
import com.nexora.core.infrastructure.persistence.notification.mappers.NotificationMapper;
import com.nexora.core.infrastructure.persistence.notification.repositories.NotificationJpaRepository;
import com.nexora.core.infrastructure.persistence.user.entities.UserJpaEntity;
import com.nexora.core.infrastructure.persistence.user.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationPersistenceAdapter implements NotificationRepository {

    private final NotificationJpaRepository notificationJpaRepository;
    private final NotificationMapper notificationMapper;
    private final UserJpaRepository userJpaRepository;

    @Override
    public List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, int limit, int offset) {
        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        int safeLimit = Math.max(1, limit);
        int page = offset / safeLimit;
        PageRequest pageRequest = PageRequest.of(page, safeLimit);
        return notificationJpaRepository.findByUserOrderByCreatedAtDesc(user, pageRequest).stream()
                .map(notificationMapper::toDomain)
                .toList();
    }

    @Override
    public long countByUserIdAndIsReadFalse(UUID userId) {
        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationJpaRepository.countByUserAndIsReadFalse(user);
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return notificationJpaRepository.findById(id).map(notificationMapper::toDomain);
    }

    @Override
    @Transactional
    public Notification save(Notification notification) {
        UserJpaEntity user = userJpaRepository.findById(notification.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserJpaEntity sender = userJpaRepository.findById(notification.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        NotificationJpaEntity entity = notificationMapper.toJpa(notification, user, sender);
        NotificationJpaEntity saved = notificationJpaRepository.save(entity);
        return notificationMapper.toDomain(saved);
    }

    @Override
    @Transactional
    public void markAsRead(UUID id) {
        notificationJpaRepository.findById(id).ifPresent(entity -> {
            entity.setIsRead(true);
            notificationJpaRepository.save(entity);
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        notificationJpaRepository.markAllAsRead(user);
    }
}

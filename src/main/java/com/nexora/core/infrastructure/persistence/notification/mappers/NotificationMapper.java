package com.nexora.core.infrastructure.persistence.notification.mappers;

import com.nexora.core.domain.notification.aggregates.Notification;
import com.nexora.core.domain.notification.valueobjects.NotificationType;
import com.nexora.core.infrastructure.persistence.notification.entities.NotificationJpaEntity;
import com.nexora.core.infrastructure.persistence.user.entities.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public Notification toDomain(NotificationJpaEntity entity) {
        if (entity == null) return null;

        return Notification.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .senderId(entity.getSender() != null ? entity.getSender().getId() : null)
                .type(entity.getType() != null ? NotificationType.valueOf(entity.getType()) : null)
                .content(entity.getContent())
                .isRead(entity.getIsRead() != null && entity.getIsRead())
                .postId(entity.getPostId())
                .eventId(entity.getEventId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public NotificationJpaEntity toJpa(Notification domain, UserJpaEntity user, UserJpaEntity sender) {
        if (domain == null) return null;

        NotificationJpaEntity entity = NotificationJpaEntity.builder()
                .user(user)
                .sender(sender)
                .type(domain.getType() != null ? domain.getType().name() : null)
                .content(domain.getContent())
                .isRead(domain.isRead())
                .postId(domain.getPostId())
                .eventId(domain.getEventId())
                .build();
        entity.setId(domain.getId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}

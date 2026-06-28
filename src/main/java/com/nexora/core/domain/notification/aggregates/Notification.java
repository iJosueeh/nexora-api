package com.nexora.core.domain.notification.aggregates;

import com.nexora.core.domain.notification.valueobjects.NotificationType;
import com.nexora.core.domain.shared.model.DomainModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends DomainModel {
    private UUID userId;
    private UUID senderId;
    private NotificationType type;
    private String content;
    private boolean isRead;
    private UUID postId;
    private UUID eventId;

    public static Notification create(UUID userId, UUID senderId, NotificationType type,
                                      String content, UUID postId, UUID eventId) {
        if (userId == null) throw new IllegalArgumentException("User ID cannot be null");
        if (senderId == null) throw new IllegalArgumentException("Sender ID cannot be null");
        if (type == null) throw new IllegalArgumentException("Type cannot be null");

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setSenderId(senderId);
        notification.setType(type);
        notification.setContent(content);
        notification.setRead(false);
        notification.setPostId(postId);
        notification.setEventId(eventId);
        return notification;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}

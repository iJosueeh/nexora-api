package com.nexora.core.domain.content.aggregates;

import com.nexora.core.domain.shared.model.DomainModel;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bookmark extends DomainModel {
    private UUID userId;
    private UUID postId;

    public static Bookmark create(UUID userId, UUID postId) {
        Bookmark bookmark = new Bookmark();
        bookmark.setUserId(userId);
        bookmark.setPostId(postId);
        return bookmark;
    }
}

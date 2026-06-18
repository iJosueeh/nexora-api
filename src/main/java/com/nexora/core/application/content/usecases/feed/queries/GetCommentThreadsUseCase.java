package com.nexora.core.application.content.usecases.feed.queries;

import com.nexora.core.infrastructure.persistence.content.adapters.FeedQueryJdbcAdapter;
import com.nexora.core.application.content.dto.CommentThreadView;
import com.nexora.core.application.security.services.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetCommentThreadsUseCase {

    private final FeedQueryJdbcAdapter feedQueryJdbcAdapter;
    private final SecurityService securityService;

    public List<CommentThreadView> execute(UUID postId) {
        UUID currentUserId = getCurrentUserIdSafe();
        List<CommentThreadView> comentariosViews = feedQueryJdbcAdapter.queryCommentThreads(postId, currentUserId);

        Map<UUID, CommentThreadView> porId = new HashMap<>();
        for (CommentThreadView comentario : comentariosViews) {
            porId.put(comentario.id(), comentario);
        }

        List<CommentThreadView> raices = new ArrayList<>();
        for (CommentThreadView comentario : comentariosViews) {
            UUID parentId = comentario.parentId();
            if (parentId != null && porId.containsKey(parentId)) {
                porId.get(parentId).respuestas().add(comentario);
            } else {
                raices.add(comentario);
            }
        }
        return raices;
    }

    private UUID getCurrentUserIdSafe() {
        try {
            return securityService.getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }
}

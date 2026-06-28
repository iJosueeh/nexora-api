package com.nexora.core.application.content.usecases.bookmarks.queries;

import com.nexora.core.application.security.services.SecurityService;
import com.nexora.core.domain.content.aggregates.Post;
import com.nexora.core.domain.content.repositories.BookmarkRepository;
import com.nexora.core.domain.content.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetBookmarksUseCase {

    private final BookmarkRepository bookmarkRepository;
    private final PostRepository postRepository;
    private final SecurityService securityService;

    public List<Post> execute(int limit, int offset) {
        UUID userId = securityService.getCurrentUserId();
        List<UUID> postIds = bookmarkRepository.findPostIdsByUserId(userId, limit, offset);
        if (postIds.isEmpty()) {
            return Collections.emptyList();
        }
        return postRepository.findAllByIdIn(postIds);
    }
}

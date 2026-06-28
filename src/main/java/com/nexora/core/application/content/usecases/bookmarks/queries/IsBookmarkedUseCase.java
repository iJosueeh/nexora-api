package com.nexora.core.application.content.usecases.bookmarks.queries;

import com.nexora.core.application.security.services.SecurityService;
import com.nexora.core.domain.content.repositories.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IsBookmarkedUseCase {

    private final BookmarkRepository bookmarkRepository;
    private final SecurityService securityService;

    public boolean execute(UUID postId) {
        UUID userId = securityService.getCurrentUserId();
        return bookmarkRepository.existsByUserIdAndPostId(userId, postId);
    }
}

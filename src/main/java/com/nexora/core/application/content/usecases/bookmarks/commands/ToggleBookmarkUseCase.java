package com.nexora.core.application.content.usecases.bookmarks.commands;

import com.nexora.core.application.security.services.SecurityService;
import com.nexora.core.domain.content.aggregates.Bookmark;
import com.nexora.core.domain.content.repositories.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ToggleBookmarkUseCase {

    private final BookmarkRepository bookmarkRepository;
    private final SecurityService securityService;

    public boolean execute(UUID postId) {
        UUID userId = securityService.getCurrentUserId();

        if (bookmarkRepository.existsByUserIdAndPostId(userId, postId)) {
            bookmarkRepository.deleteByUserIdAndPostId(userId, postId);
            return false;
        } else {
            bookmarkRepository.save(Bookmark.create(userId, postId));
            return true;
        }
    }
}

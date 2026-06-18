package com.nexora.core.presentation.graphql.content;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.nexora.core.application.content.dto.FeedPostView;
import com.nexora.core.application.content.dto.FeedAuthorView;
import com.nexora.core.application.content.usecases.bookmarks.commands.ToggleBookmarkUseCase;
import com.nexora.core.application.content.usecases.bookmarks.queries.GetBookmarksUseCase;
import com.nexora.core.application.content.usecases.bookmarks.queries.IsBookmarkedUseCase;
import com.nexora.core.domain.content.aggregates.Post;
import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import com.nexora.core.application.security.services.SecurityService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BookmarksGraphQlController {

    private final ToggleBookmarkUseCase toggleBookmarkUseCase;
    private final GetBookmarksUseCase getBookmarksUseCase;
    private final IsBookmarkedUseCase isBookmarkedUseCase;
    private final ProfileRepository profileRepository;
    private final SecurityService securityService;

    @MutationMapping
    public boolean toggleBookmark(@Argument UUID postId) {
        return toggleBookmarkUseCase.execute(postId);
    }

    @QueryMapping
    public List<FeedPostView> bookmarks(@Argument int limit, @Argument int offset) {
        List<Post> posts = getBookmarksUseCase.execute(limit, offset);
        UUID currentUserId = securityService.getCurrentUserId();
        List<UUID> authorIds = posts.stream().map(p -> p.getAutor().getId()).distinct().toList();
        Map<UUID, Profile> profileMap = profileRepository.findByUserIdIn(authorIds).stream()
                .collect(Collectors.toMap(Profile::getUserId, p -> p));

        return posts.stream().map(post -> {
            Profile profile = profileMap.get(post.getAutor().getId());
            FeedAuthorView autor = new FeedAuthorView(
                    post.getAutor().getId(),
                    profile != null && profile.getUsername() != null ? profile.getUsername().value() : null,
                    profile != null && profile.getFullName() != null ? profile.getFullName().value() : "Sin nombre",
                    profile != null ? profile.getAvatarUrl() : null
            );
            return new FeedPostView(
                    post.getId(),
                    post.getTitulo(),
                    post.getContent(),
                    Boolean.TRUE.equals(post.getIsOfficial()),
                    post.getCreatedAt() != null ? post.getCreatedAt().atOffset(java.time.ZoneOffset.UTC) : null,
                    0, 0, false,
                    autor,
                    post.getTags() == null ? List.of() : List.copyOf(post.getTags()),
                    post.getLocation(),
                    post.getImageUrl()
            );
        }).toList();
    }

    @QueryMapping
    public boolean isBookmarked(@Argument UUID postId) {
        return isBookmarkedUseCase.execute(postId);
    }
}

package com.nexora.core.presentation.graphql;

import java.util.List;
import java.util.UUID;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import com.nexora.core.application.content.dto.CommentThreadView;
import com.nexora.core.application.content.dto.FeedPostView;
import com.nexora.core.application.content.dto.TagSuggestionView;
import com.nexora.core.application.content.dto.TrendingTopicView;
import com.nexora.core.application.auth.dto.ProfileView;
import com.nexora.core.application.content.usecases.social.queries.GetFollowersUseCase;
import com.nexora.core.application.content.usecases.social.queries.GetFollowingUseCase;
import com.nexora.core.application.content.usecases.feed.queries.GetFeedUseCase;
import com.nexora.core.application.content.usecases.feed.queries.GetUserPostsUseCase;
import com.nexora.core.application.content.usecases.feed.queries.GetPostByIdUseCase;
import com.nexora.core.application.content.usecases.feed.queries.GetCommentThreadsUseCase;
import com.nexora.core.application.content.usecases.feed.queries.GetTagsUseCase;
import com.nexora.core.application.content.usecases.feed.queries.GetTrendingTopicsUseCase;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class NexoraQueryController {

    private static final int MAX_OFFSET = 10_000;
    private static final int MAX_TAG_LIMIT = 30;

    private final GetFeedUseCase getFeedUseCase;
    private final GetUserPostsUseCase getUserPostsUseCase;
    private final GetPostByIdUseCase getPostByIdUseCase;
    private final GetCommentThreadsUseCase getCommentThreadsUseCase;
    private final GetTagsUseCase getTagsUseCase;
    private final GetTrendingTopicsUseCase getTrendingTopicsUseCase;
    private final GetFollowersUseCase getFollowersUseCase;
    private final GetFollowingUseCase getFollowingUseCase;

    @QueryMapping
    public List<ProfileView> followers(@Argument UUID userId) {
        return getFollowersUseCase.execute(userId);
    }

    @QueryMapping
    public List<ProfileView> following(@Argument UUID userId) {
        return getFollowingUseCase.execute(userId);
    }

    @QueryMapping
    public String health() {
        return "Nexora GraphQL API is running";
    }

    @QueryMapping
    public List<FeedPostView> obtenerFeedPrincipal(@Argument Integer limit, @Argument Integer offset) {
        int safeLimit = limit == null ? 20 : Math.max(1, Math.min(limit, 100));
        int safeOffset = offset == null ? 0 : Math.max(0, Math.min(offset, MAX_OFFSET));
        return getFeedUseCase.execute(safeLimit, safeOffset);
    }

    @QueryMapping
    public FeedPostView obtenerPublicacionPorId(@Argument UUID postId) {
        if (postId == null) return null;
        return getPostByIdUseCase.execute(postId);
    }

    @QueryMapping
    public List<FeedPostView> publicacionesPorUsuario(@Argument String username, @Argument Integer limit,
            @Argument Integer offset) {
        int safeLimit = limit == null ? 20 : Math.max(1, Math.min(limit, 100));
        int safeOffset = offset == null ? 0 : Math.max(0, Math.min(offset, MAX_OFFSET));
        String safeUsername = username == null ? "" : username.trim().toLowerCase();

        if (safeUsername.isBlank()) {
            return List.of();
        }

        return getUserPostsUseCase.execute(safeUsername, safeLimit, safeOffset);
    }

    @QueryMapping
    public List<CommentThreadView> comentariosPorPost(@Argument UUID postId) {
        return getCommentThreadsUseCase.execute(postId);
    }

    @SchemaMapping(typeName = "CommentThread", field = "respuestas")
    public List<CommentThreadView> respuestas(CommentThreadView commentThread) {
        return commentThread.respuestas();
    }

    @QueryMapping
    public List<TagSuggestionView> availableTags(@Argument String search, @Argument Integer limit) {
        int safeLimit = limit == null ? 12 : Math.max(1, Math.min(limit, MAX_TAG_LIMIT));
        String safeSearch = search == null ? "" : search.trim().toLowerCase();
        return getTagsUseCase.execute(safeSearch, safeLimit);
    }

    @QueryMapping
    public List<TrendingTopicView> trendingTopics(@Argument Integer limit) {
        int safeLimit = limit == null ? 10 : Math.max(1, Math.min(limit, 50));
        return getTrendingTopicsUseCase.execute(safeLimit);
    }
}

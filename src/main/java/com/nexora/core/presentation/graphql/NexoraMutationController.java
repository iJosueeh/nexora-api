package com.nexora.core.presentation.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import com.nexora.core.application.auth.usecases.commands.UpdateProfileUseCase;
import com.nexora.core.application.content.usecases.interaction.commands.TogglePostLikeUseCase;
import com.nexora.core.application.content.usecases.interaction.commands.ToggleCommentLikeUseCase;
import com.nexora.core.application.content.usecases.social.commands.ToggleFollowUseCase;
import com.nexora.core.application.content.usecases.feed.commands.CreatePostUseCase;
import com.nexora.core.application.content.usecases.feed.commands.EditPostUseCase;
import com.nexora.core.application.content.usecases.feed.commands.CreateCommentUseCase;
import com.nexora.core.application.content.usecases.feed.commands.EditCommentUseCase;
import com.nexora.core.application.content.usecases.feed.commands.DeleteCommentUseCase;
import com.nexora.core.application.content.dto.CommentThreadView;
import com.nexora.core.presentation.graphql.dto.CreateCommentInput;
import com.nexora.core.presentation.graphql.dto.CreatePublicationInput;
import com.nexora.core.presentation.graphql.dto.UpdatePublicationInput;
import com.nexora.core.application.content.dto.FeedPostView;
import com.nexora.core.application.auth.dto.ProfileView;
import com.nexora.core.presentation.graphql.dto.UpdateProfileInput;
import com.nexora.core.application.content.dto.FeedAuthorView;
import com.nexora.core.domain.content.aggregates.Post;
import com.nexora.core.domain.content.aggregates.Comment;
import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.repositories.ProfileRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class NexoraMutationController {

    private final CreatePostUseCase createPostUseCase;
    private final EditPostUseCase editPostUseCase;
    private final CreateCommentUseCase createCommentUseCase;
    private final EditCommentUseCase editCommentUseCase;
    private final DeleteCommentUseCase deleteCommentUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final TogglePostLikeUseCase togglePostLikeUseCase;
    private final ToggleCommentLikeUseCase toggleCommentLikeUseCase;
    private final ToggleFollowUseCase toggleFollowUseCase;
    private final ProfileRepository profileRepository;

    @MutationMapping
    public FeedPostView crearPublicacion(@AuthenticationPrincipal Jwt jwt, @Argument CreatePublicationInput input) {
        String email = jwt.getClaimAsString("email");
        Post post = createPostUseCase.execute(email, input.titulo(), input.contenido(), input.tags(), input.location(), input.imageUrl());
        return toFeedPostView(post);
    }

    @MutationMapping
    public FeedPostView editarPublicacion(@AuthenticationPrincipal Jwt jwt, @Argument UUID postId, @Argument UpdatePublicationInput input) {
        String email = jwt.getClaimAsString("email");
        Post post = editPostUseCase.execute(email, postId, input.titulo(), input.contenido(), input.tags(), input.location(), input.imageUrl());
        return toFeedPostView(post);
    }

    @MutationMapping
    public CommentThreadView crearComentario(@AuthenticationPrincipal Jwt jwt, @Argument CreateCommentInput input) {
        String email = jwt.getClaimAsString("email");
        Comment comment = createCommentUseCase.execute(email, input.postId(), input.parentId(), input.contenido());
        return toCommentThreadView(comment);
    }

    @MutationMapping
    public CommentThreadView editarComentario(@AuthenticationPrincipal Jwt jwt, @Argument UUID commentId, @Argument String contenido) {
        String email = jwt.getClaimAsString("email");
        Comment comment = editCommentUseCase.execute(email, commentId, contenido);
        return toCommentThreadView(comment);
    }

    @MutationMapping
    public boolean eliminarComentario(@AuthenticationPrincipal Jwt jwt, @Argument UUID commentId) {
        String email = jwt.getClaimAsString("email");
        return deleteCommentUseCase.execute(email, commentId);
    }

    @MutationMapping
    public ProfileView actualizarPerfil(@AuthenticationPrincipal Jwt jwt, @Argument UpdateProfileInput input) {
        String email = jwt.getClaimAsString("email");
        return updateProfileUseCase.execute(email, input);
    }

    @MutationMapping
    public boolean toggleLike(@Argument UUID postId) {
        return togglePostLikeUseCase.execute(postId);
    }

    @MutationMapping
    public boolean toggleFollow(@Argument UUID targetUserId) {
        return toggleFollowUseCase.execute(targetUserId);
    }

    @MutationMapping
    public boolean toggleCommentLike(@Argument UUID commentId) {
        return toggleCommentLikeUseCase.execute(commentId);
    }

    private FeedPostView toFeedPostView(Post post) {
        Profile profile = profileRepository.findByUserId(post.getAutor().getId()).orElse(null);
        String username = profile != null ? profile.getUsername().value() : post.getAutor().getEmail().username();
        String fullName = profile != null && profile.getFullName() != null
                ? profile.getFullName().value()
                : username;
        String avatarUrl = profile != null && profile.getAvatarUrl() != null && !profile.getAvatarUrl().isBlank()
                ? profile.getAvatarUrl()
                : null;

        FeedAuthorView autor = new FeedAuthorView(
                post.getAutor().getId(),
                username,
                fullName,
                avatarUrl);

        OffsetDateTime createdAt = post.getCreatedAt() == null
                ? OffsetDateTime.now(ZoneOffset.UTC)
                : post.getCreatedAt().atOffset(ZoneOffset.UTC);

        return new FeedPostView(
                post.getId(),
                post.getTitulo(),
                post.getContent(),
                Boolean.TRUE.equals(post.getIsOfficial()),
                createdAt,
                0, 0, false,
                autor,
                post.getTags() == null ? List.of() : List.copyOf(post.getTags()),
                post.getLocation(),
                post.getImageUrl());
    }

    private CommentThreadView toCommentThreadView(Comment comment) {
        Profile profile = profileRepository.findByUserId(comment.getAutor().getId()).orElse(null);
        FeedAuthorView autor = new FeedAuthorView(
                comment.getAutor().getId(),
                profile != null ? profile.getUsername().value() : comment.getAutor().getEmail().username(),
                profile != null && profile.getFullName() != null ? profile.getFullName().value() : comment.getAutor().getEmail().value(),
                profile != null ? profile.getAvatarUrl() : null
        );

        return new CommentThreadView(
                comment.getId(),
                comment.getPost().getId(),
                comment.getParent() != null ? comment.getParent().getId() : null,
                autor,
                comment.getContent(),
                comment.getCreatedAt().atOffset(ZoneOffset.UTC)
        );
    }
}

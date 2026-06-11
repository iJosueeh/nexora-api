package com.nexora.core.presentation.graphql;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.core.domain.content.aggregates.Comment;
import com.nexora.core.domain.content.aggregates.Post;
import com.nexora.core.domain.content.repositories.CommentRepository;
import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.repositories.UserRepository;
import com.nexora.core.domain.user.valueobjects.Email;
import com.nexora.core.domain.content.repositories.PostRepository;
import com.nexora.core.presentation.graphql.dto.CommentThreadView;
import com.nexora.core.presentation.graphql.dto.CreateCommentInput;
import com.nexora.core.presentation.graphql.dto.CreatePublicationInput;
import com.nexora.core.presentation.graphql.dto.UpdatePublicationInput;
import com.nexora.core.presentation.graphql.dto.FeedAuthorView;
import com.nexora.core.presentation.graphql.dto.FeedPostView;
import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.repositories.ProfileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedMutationService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public CommentThreadView crearComentario(Jwt jwt, CreateCommentInput input) {
        String email = jwt.getClaimAsString("email");
        User user = userRepository.findByEmail(new Email(email.trim()))
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);

        FeedAuthorView autor = new FeedAuthorView(
                user.getId(),
                profile != null ? profile.getUsername().value() : user.getEmail().username(),
                profile != null && profile.getFullName() != null ? profile.getFullName().value() : user.getEmail().value(),
                profile != null ? profile.getAvatarUrl() : null
        );

        Post post = postRepository.findById(input.postId())
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Comment comment;
        if (input.parentId() != null) {
            Comment parent = commentRepository.findById(input.parentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
            comment = Comment.createReply(post, user, input.contenido(), parent);
        } else {
            comment = Comment.create(post, user, input.contenido());
        }

        Comment saved = commentRepository.save(comment);
        
        return new CommentThreadView(
                saved.getId(),
                saved.getPost().getId(),
                saved.getParent() != null ? saved.getParent().getId() : null,
                autor,
                saved.getContent(),
                saved.getCreatedAt().atOffset(ZoneOffset.UTC)
        );
    }

    @Transactional
    public CommentThreadView editarComentario(Jwt jwt, UUID commentId, String contenido) {
        String email = jwt.getClaimAsString("email");
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        if (!comment.getAutor().getEmail().value().equalsIgnoreCase(email.trim())) {
            throw new IllegalStateException("Only the author can edit this comment");
        }

        comment.updateContent(contenido);
        Comment updated = commentRepository.save(comment);

        Profile profile = profileRepository.findByUserId(updated.getAutor().getId()).orElse(null);

        FeedAuthorView autor = new FeedAuthorView(
                updated.getAutor().getId(),
                profile != null ? profile.getUsername().value() : updated.getAutor().getEmail().username(),
                profile != null && profile.getFullName() != null ? profile.getFullName().value() : updated.getAutor().getEmail().value(),
                profile != null ? profile.getAvatarUrl() : null
        );

        return new CommentThreadView(
                updated.getId(),
                updated.getPost().getId(),
                updated.getParent() != null ? updated.getParent().getId() : null,
                autor,
                updated.getContent(),
                updated.getCreatedAt().atOffset(ZoneOffset.UTC)
        );
    }

    @Transactional
    public boolean eliminarComentario(Jwt jwt, UUID commentId) {
        String email = jwt.getClaimAsString("email");
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        if (!comment.getAutor().getEmail().value().equalsIgnoreCase(email.trim())) {
            throw new IllegalStateException("Only the author can delete this comment");
        }

        commentRepository.delete(comment);
        return true;
    }

    @Transactional
    public FeedPostView crearPublicacion(Jwt jwt, CreatePublicationInput input) {
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Authenticated user email is required");
        }

        String content = input.contenido() == null ? "" : input.contenido().trim();
        if (content.isBlank()) {
            throw new IllegalArgumentException("Publication content is required");
        }

        User user = userRepository.findByEmail(new Email(email.trim()))
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        Post post = Post.create(user, input.titulo(), content, resolveTags(input.tags()), input.location(), input.imageUrl());

        Post savedPost = postRepository.save(post);
        return toView(savedPost, resolveProfile(user));
    }

    @Transactional
    public FeedPostView editarPublicacion(Jwt jwt, UUID postId, UpdatePublicationInput input) {
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Authenticated user email is required");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (!post.getAutor().getEmail().value().equalsIgnoreCase(email.trim())) {
            throw new IllegalStateException("Only the author can edit this publication");
        }

        String content = input.contenido() == null ? "" : input.contenido().trim();
        if (content.isBlank()) {
            throw new IllegalArgumentException("Publication content is required");
        }

        post.update(input.titulo(), content, resolveTags(input.tags()), input.location(), input.imageUrl());

        Post updatedPost = postRepository.save(post);
        return toView(updatedPost, resolveProfile(updatedPost.getAutor()));
    }

    private FeedPostView toView(Post post, Profile profile) {
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
                0, // commentsCount initial
                0, // likesCount initial
                false, // isLiked initial
                autor,
                post.getTags() == null ? List.of() : List.copyOf(post.getTags()),
                post.getLocation(),
                post.getImageUrl());
    }

    private Profile resolveProfile(User user) {
        return profileRepository.findByUserId(user.getId()).orElse(null);
    }

    private String resolveTitle(String title, String content) {
        if (title != null && !title.isBlank()) {
            return title.trim();
        }

        String firstLine = content.split("\\n")[0].trim();
        return firstLine.length() > 90 ? firstLine.substring(0, 90) : firstLine;
    }

    private String resolveLocation(String location) {
        if (location == null) {
            return null;
        }

        String trimmed = location.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed.length() > 120 ? trimmed.substring(0, 120) : trimmed;
    }

    private List<String> resolveTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }

        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String raw : tags) {
            if (raw == null) {
                continue;
            }

            String tag = raw.replaceFirst("^#", "").trim().toLowerCase(Locale.ROOT);
            if (!tag.isEmpty()) {
                normalized.add(tag);
            }

            if (normalized.size() >= 8) {
                break;
            }
        }

        return new ArrayList<>(normalized);
    }
}

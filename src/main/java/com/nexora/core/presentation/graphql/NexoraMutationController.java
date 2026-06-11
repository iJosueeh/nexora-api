package com.nexora.core.presentation.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import com.nexora.core.application.auth.services.AuthService;
import com.nexora.core.application.content.services.InteractionService;
import com.nexora.core.application.content.services.SocialService;
import com.nexora.core.presentation.graphql.dto.CommentThreadView;
import com.nexora.core.presentation.graphql.dto.CreateCommentInput;
import com.nexora.core.presentation.graphql.dto.CreatePublicationInput;
import com.nexora.core.presentation.graphql.dto.UpdatePublicationInput;
import com.nexora.core.presentation.graphql.dto.FeedPostView;
import com.nexora.core.presentation.graphql.dto.ProfileView;
import com.nexora.core.presentation.graphql.dto.UpdateProfileInput;

import java.util.UUID;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class NexoraMutationController {

    private final FeedMutationService feedMutationService;
    private final AuthService authService;
    private final InteractionService interactionService;
    private final SocialService socialService;

    @MutationMapping
    public FeedPostView crearPublicacion(@AuthenticationPrincipal Jwt jwt, @Argument CreatePublicationInput input) {
        return feedMutationService.crearPublicacion(jwt, input);
    }

    @MutationMapping
    public FeedPostView editarPublicacion(@AuthenticationPrincipal Jwt jwt, @Argument UUID postId, @Argument UpdatePublicationInput input) {
        return feedMutationService.editarPublicacion(jwt, postId, input);
    }

    @MutationMapping
    public CommentThreadView crearComentario(@AuthenticationPrincipal Jwt jwt, @Argument CreateCommentInput input) {
        return feedMutationService.crearComentario(jwt, input);
    }

    @MutationMapping
    public CommentThreadView editarComentario(@AuthenticationPrincipal Jwt jwt, @Argument UUID commentId, @Argument String contenido) {
        return feedMutationService.editarComentario(jwt, commentId, contenido);
    }

    @MutationMapping
    public boolean eliminarComentario(@AuthenticationPrincipal Jwt jwt, @Argument UUID commentId) {
        return feedMutationService.eliminarComentario(jwt, commentId);
    }

    @MutationMapping
    public ProfileView actualizarPerfil(@AuthenticationPrincipal Jwt jwt, @Argument UpdateProfileInput input) {
        String email = jwt.getClaimAsString("email");
        return authService.actualizarPerfil(email, input);
    }

    @MutationMapping
    public boolean toggleLike(@Argument UUID postId) {
        return interactionService.toggleLike(postId);
    }

    @MutationMapping
    public boolean toggleFollow(@Argument UUID targetUserId) {
        return socialService.toggleFollow(targetUserId);
    }

    @MutationMapping
    public boolean toggleCommentLike(@Argument UUID commentId) {
        return interactionService.toggleCommentLike(commentId);
    }
}

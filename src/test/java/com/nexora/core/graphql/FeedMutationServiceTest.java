package com.nexora.core.presentation.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import com.nexora.core.domain.content.aggregates.Post;
import com.nexora.core.domain.content.repositories.PostRepository;
import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.repositories.UserRepository;
import com.nexora.core.domain.user.valueobjects.Email;
import com.nexora.core.domain.user.valueobjects.UserRole;
import com.nexora.core.presentation.graphql.dto.UpdatePublicationInput;
import com.nexora.core.domain.user.repositories.ProfileRepository;

@ExtendWith(MockitoExtension.class)
class FeedMutationServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private Jwt jwt;

    @InjectMocks
    private FeedMutationService feedMutationService;

    private User author;
    private Post post;
    private UUID postId;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        author = User.builder()
                .email(new Email("author@utp.edu.pe"))
                .id(UUID.randomUUID())
                .isActive(true)
                .role(UserRole.ROLE_STUDENT)
                .build();

        post = new Post();
        post.setId(postId);
        post.setAutor(author);
        post.setContent("Original content");
    }

    @Test
    void editarPublicacion_Success() {
        when(jwt.getClaimAsString("email")).thenReturn("author@utp.edu.pe");
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any())).thenReturn(post);

        UpdatePublicationInput input = new UpdatePublicationInput(
                "New Title", "Updated Content", List.of("tag1"), "Location", null
        );

        assertDoesNotThrow(() -> feedMutationService.editarPublicacion(jwt, postId, input));
        
        verify(postRepository).save(post);
        assertEquals("Updated Content", post.getContent());
    }

    @Test
    void editarPublicacion_Forbidden_WhenNotAuthor() {
        when(jwt.getClaimAsString("email")).thenReturn("other@utp.edu.pe");
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        UpdatePublicationInput input = new UpdatePublicationInput(
                "Title", "Content", null, null, null
        );

        assertThrows(IllegalStateException.class, () -> 
                feedMutationService.editarPublicacion(jwt, postId, input)
        );
        
        verify(postRepository, never()).save(any());
    }

    @Test
    void editarPublicacion_NotFound() {
        when(jwt.getClaimAsString("email")).thenReturn("author@utp.edu.pe");
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        UpdatePublicationInput input = new UpdatePublicationInput("T", "C", null, null, null);

        assertThrows(IllegalArgumentException.class, () -> 
                feedMutationService.editarPublicacion(jwt, postId, input)
        );
    }
}

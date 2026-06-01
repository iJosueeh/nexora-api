package com.nexora.core.graphql;

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

import com.nexora.core.content.entity.Post;
import com.nexora.core.content.repository.PostRepository;
import com.nexora.core.graphql.dto.UpdatePublicationInput;
import com.nexora.core.profile.repository.ProfilesRepository;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FeedMutationServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfilesRepository profilesRepository;
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
        author = new User();
        author.setEmail("author@utp.edu.pe");
        author.setId(UUID.randomUUID());

        post = new Post();
        post.setId(postId);
        post.setAutor(author);
        post.setContent("Original content");
    }

    @Test
    void editarPublicacion_Success() {
        when(jwt.getClaimAsString("email")).thenReturn("author@utp.edu.pe");
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.saveAndFlush(any())).thenReturn(post);

        UpdatePublicationInput input = new UpdatePublicationInput(
                "New Title", "Updated Content", List.of("tag1"), "Location", null
        );

        assertDoesNotThrow(() -> feedMutationService.editarPublicacion(jwt, postId, input));
        
        verify(postRepository).saveAndFlush(post);
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
        
        verify(postRepository, never()).saveAndFlush(any());
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

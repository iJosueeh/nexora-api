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

import com.nexora.core.application.content.usecases.feed.commands.EditPostUseCase;
import com.nexora.core.domain.content.aggregates.Post;
import com.nexora.core.domain.content.repositories.PostRepository;
import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.valueobjects.Email;
import com.nexora.core.domain.user.valueobjects.UserRole;

@ExtendWith(MockitoExtension.class)
class FeedMutationServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private EditPostUseCase editPostUseCase;

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
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any())).thenReturn(post);

        assertDoesNotThrow(() -> editPostUseCase.execute("author@utp.edu.pe", postId,
                "New Title", "Updated Content", List.of("tag1"), "Location", null));

        verify(postRepository).save(post);
        assertEquals("Updated Content", post.getContent());
    }

    @Test
    void editarPublicacion_Forbidden_WhenNotAuthor() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        assertThrows(IllegalStateException.class, () ->
                editPostUseCase.execute("other@utp.edu.pe", postId,
                        "Title", "Content", null, null, null)
        );

        verify(postRepository, never()).save(any());
    }

    @Test
    void editarPublicacion_NotFound() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                editPostUseCase.execute("author@utp.edu.pe", postId,
                        "T", "C", null, null, null)
        );
    }
}

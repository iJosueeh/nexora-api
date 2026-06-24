package com.nexora.core.application.content.usecases.feed.commands;

import com.nexora.core.domain.content.aggregates.Post;
import com.nexora.core.domain.content.repositories.PostRepository;
import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.repositories.UserRepository;
import com.nexora.core.domain.user.valueobjects.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class CreatePostUseCase {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Post execute(String email, String titulo, String contenido, List<String> tags, String location, String imageUrl) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Authenticated user email is required");
        }

        String content = contenido == null ? "" : contenido.trim();
        if (content.isBlank()) {
            throw new IllegalArgumentException("Publication content is required");
        }

        User user = userRepository.findByEmail(new Email(email.trim()))
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        Post post = Post.create(user, titulo, content, resolveTags(tags), location, imageUrl);
        return postRepository.save(post);
    }

    private List<String> resolveTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }

        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String raw : tags) {
            if (raw == null) continue;
            String tag = raw.replaceFirst("^#", "").trim().toLowerCase(Locale.ROOT);
            if (!tag.isEmpty()) {
                normalized.add(tag);
            }
            if (normalized.size() >= 8) break;
        }

        return new ArrayList<>(normalized);
    }
}

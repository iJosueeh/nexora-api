package com.nexora.core.application.content.usecases.feed.queries;

import com.nexora.core.domain.content.aggregates.Post;
import com.nexora.core.domain.content.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchPostsUseCase {

    private final PostRepository postRepository;

    public List<Post> execute(String query, int limit, int offset) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return postRepository.searchByFullText(query.trim(), limit, offset);
    }
}

package com.nexora.core.domain.content.aggregates;

import com.nexora.core.domain.shared.model.DomainModel;
import com.nexora.core.domain.user.aggregates.User;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import lombok.Builder;
import java.util.Locale;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Post extends DomainModel {
    private User autor;
    private UUID tipoId;
    private String titulo;
    private String content;
    @Builder.Default
    private Boolean isOfficial = false;
    @Builder.Default
    private String status = "PUBLISHED";
    private String location;
    private String imageUrl;
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    public static Post create(User author, String title, String content, List<String> tags, String location, String imageUrl) {
        validateContent(content);
        
        Post post = new Post();
        post.setAutor(author);
        post.updateContent(title, content);
        post.updateTags(tags);
        post.setLocation(normalizeLocation(location));
        post.setImageUrl(imageUrl);
        post.setIsOfficial(false);
        post.setStatus("PUBLISHED");
        return post;
    }

    public void update(String title, String content, List<String> tags, String location, String imageUrl) {
        validateContent(content);
        updateContent(title, content);
        updateTags(tags);
        this.location = normalizeLocation(location);
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
    }

    public void updateContent(String rawTitle, String content) {
        validateContent(content);
        this.content = content.trim();
        this.titulo = resolveTitle(rawTitle, this.content);
    }

    public void updateTags(List<String> rawTags) {
        if (rawTags == null || rawTags.isEmpty()) {
            this.tags = new ArrayList<>();
            return;
        }

        List<String> normalized = new ArrayList<>();
        for (String raw : rawTags) {
            if (raw == null) continue;
            String tag = raw.replaceFirst("^#", "").trim().toLowerCase(Locale.ROOT);
            if (!tag.isEmpty() && !normalized.contains(tag)) {
                normalized.add(tag);
            }
            if (normalized.size() >= 8) break;
        }
        this.tags = normalized;
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Post content cannot be empty");
        }
    }

    private String resolveTitle(String title, String content) {
        if (title != null && !title.isBlank()) {
            return title.trim();
        }
        String firstLine = content.split("\\n")[0].trim();
        return firstLine.length() > 90 ? firstLine.substring(0, 90) : firstLine;
    }

    private static String normalizeLocation(String location) {
        if (location == null || location.isBlank()) return null;
        String trimmed = location.trim();
        return trimmed.length() > 120 ? trimmed.substring(0, 120) : trimmed;
    }
}

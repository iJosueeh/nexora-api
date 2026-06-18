package com.nexora.core.application.content.usecases.feed.queries;

import com.nexora.core.infrastructure.persistence.content.adapters.FeedQueryJdbcAdapter;
import com.nexora.core.application.content.dto.FeedPostView;
import com.nexora.core.application.security.services.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GetFeedUseCase {

    private final FeedQueryJdbcAdapter feedQueryJdbcAdapter;
    private final SecurityService securityService;

    private static final String FEED_SELECT_BASE_SQL = """
            SELECT
                p.id,
                p.titulo,
                p.content AS contenido,
                p.location,
                COALESCE(p.is_official, FALSE) AS is_official,
                p.created_at,
                (
                    SELECT COUNT(*)
                    FROM comentarios c
                    WHERE c.post_id = p.id
                ) AS comments_count,
                (
                    SELECT COUNT(*)
                    FROM post_likes l
                    WHERE l.post_id = p.id
                ) AS likes_count,
                EXISTS (
                    SELECT 1
                    FROM post_likes l
                    WHERE l.post_id = p.id AND l.user_id = :currentUserId
                ) AS is_liked,
                u.id AS autor_id,
                pf.username AS autor_username,
                pf.full_name AS autor_full_name,
                pf.avatar_url AS autor_avatar_url,
                p.image_url
            FROM posts p
            JOIN usuarios u ON u.id = p.autor_id
            LEFT JOIN perfiles pf ON pf.usuario_id = u.id
            """;

    public List<FeedPostView> execute(int limit, int offset) {
        String sql = FEED_SELECT_BASE_SQL + """
                ORDER BY p.created_at DESC
                LIMIT :limit OFFSET :offset
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("offset", offset);

        return fetchAndEnrichFeedPosts(sql, params);
    }

    private List<FeedPostView> fetchAndEnrichFeedPosts(String sql, MapSqlParameterSource params) {
        UUID currentUserId = getCurrentUserIdSafe();

        List<FeedPostView> posts = feedQueryJdbcAdapter.fetchFeedPosts(sql, params, currentUserId);

        if (posts.isEmpty()) {
            return posts;
        }

        try {
            List<UUID> postIds = posts.stream().map(FeedPostView::id).toList();
            Map<UUID, List<String>> tagsByPost = feedQueryJdbcAdapter.queryTagsByPostIds(postIds);

            return posts.stream()
                    .map(post -> new FeedPostView(
                            post.id(), post.titulo(), post.contenido(), post.isOfficial(),
                            post.createdAt(), post.commentsCount(), post.likesCount(), post.isLiked(),
                            post.autor(),
                            tagsByPost.getOrDefault(post.id(), extractHashtags(post.titulo(), post.contenido())),
                            post.location(), post.imageUrl()))
                    .toList();
        } catch (Exception e) {
            log.warn("[GetFeedUseCase] Could not enrich posts with tags; returning posts without tags: {}", e.getMessage(), e);
            return posts;
        }
    }

    private List<String> extractHashtags(String title, String content) {
        String source = ((title == null ? "" : title) + " " + (content == null ? "" : content)).trim();
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("#[\\p{L}\\p{N}_]+").matcher(source);
        LinkedHashMap<String, Boolean> unique = new LinkedHashMap<>();
        while (matcher.find() && unique.size() < 5) {
            String tag = matcher.group().replaceFirst("^#", "").toLowerCase();
            if (!tag.isBlank()) unique.put(tag, Boolean.TRUE);
        }
        return new ArrayList<>(unique.keySet());
    }

    private UUID getCurrentUserIdSafe() {
        try {
            return securityService.getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }
}

package com.nexora.core.infrastructure.persistence.content.adapters;

import com.nexora.core.application.content.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FeedQueryJdbcAdapter {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<FeedPostView> fetchFeedPosts(String sql, MapSqlParameterSource params, UUID currentUserId) {
        params.addValue("currentUserId", currentUserId);

        try {
            return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
                Timestamp rawCreatedAt = rs.getTimestamp("created_at");
                OffsetDateTime createdAt = rawCreatedAt != null ?
                    rawCreatedAt.toLocalDateTime().atOffset(ZoneOffset.UTC) : null;

                String autorUsername = Objects.requireNonNullElse(rs.getString("autor_username"), "");
                String autorFullName = Objects.requireNonNullElse(rs.getString("autor_full_name"), "");
                String autorAvatarUrl = Objects.requireNonNullElse(rs.getString("autor_avatar_url"), "");

                FeedAuthorView autor = new FeedAuthorView(
                    rs.getObject("autor_id", UUID.class),
                    autorUsername,
                    autorFullName,
                    autorAvatarUrl);

                String titulo = Objects.requireNonNullElse(rs.getString("titulo"), "");
                String contenido = Objects.requireNonNullElse(rs.getString("contenido"), "");
                String location = Objects.requireNonNullElse(rs.getString("location"), "");
                String imageUrl = Objects.requireNonNullElse(rs.getString("image_url"), "");

                return new FeedPostView(
                    rs.getObject("id", UUID.class),
                    titulo,
                    contenido,
                    rs.getBoolean("is_official"),
                    createdAt,
                    rs.getInt("comments_count"),
                    rs.getInt("likes_count"),
                    rs.getBoolean("is_liked"),
                    autor,
                    new ArrayList<>(),
                    location,
                    imageUrl);
            });
        } catch (Exception e) {
            log.error("[FeedQueryJdbcAdapter] ERROR SQL: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<CommentThreadView> queryCommentThreads(UUID postId, UUID currentUserId) {
        String sql = """
                SELECT 
                    c.id, 
                    c.post_id, 
                    c.parent_id, 
                    c.content as contenido, 
                    c.created_at,
                    u.id as autor_id,
                    pf.username as autor_username,
                    pf.full_name as autor_full_name,
                    pf.avatar_url as autor_avatar_url,
                    (SELECT COUNT(*) FROM comment_likes cl WHERE cl.comment_id = c.id) as likes_count,
                    EXISTS(SELECT 1 FROM comment_likes cl WHERE cl.comment_id = c.id AND cl.user_id = :userId) as is_liked
                FROM comentarios c
                JOIN usuarios u ON u.id = c.autor_id
                LEFT JOIN perfiles pf ON pf.usuario_id = u.id
                WHERE c.post_id = :postId
                ORDER BY c.created_at ASC
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("postId", postId)
            .addValue("userId", currentUserId);

        try {
            return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
                Timestamp rawCreatedAt = rs.getTimestamp("created_at");
                OffsetDateTime createdAt = rawCreatedAt != null ?
                    rawCreatedAt.toLocalDateTime().atOffset(ZoneOffset.UTC) : null;

                FeedAuthorView autor = new FeedAuthorView(
                    rs.getObject("autor_id", UUID.class),
                    rs.getString("autor_username"),
                    rs.getString("autor_full_name"),
                    rs.getString("autor_avatar_url")
                );

                return new CommentThreadView(
                    rs.getObject("id", UUID.class),
                    rs.getObject("post_id", UUID.class),
                    rs.getObject("parent_id", UUID.class),
                    autor,
                    rs.getString("contenido"),
                    createdAt,
                    rs.getInt("likes_count"),
                    rs.getBoolean("is_liked")
                );
            });
        } catch (Exception e) {
            log.error("[FeedQueryJdbcAdapter] Error building comment threads: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<TagSuggestionView> queryTagSuggestions(String search, int limit) {
        String safeSearch = search == null ? "" : search.trim().toLowerCase();
        String sql = """
                SELECT tag_name, usage_count 
                FROM (
                    SELECT LOWER(TRIM(tag)) AS tag_name, COUNT(*) as usage_count 
                    FROM post_tags 
                    GROUP BY tag_name
                ) combined 
                WHERE tag_name LIKE :search
                ORDER BY usage_count DESC
                LIMIT :limit
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("search", safeSearch + "%")
            .addValue("limit", limit);

        try {
            return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
                String tagName = Objects.requireNonNullElse(rs.getString("tag_name"), "");
                return new TagSuggestionView(tagName, tagName, rs.getInt("usage_count"));
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public record PostEngagement(int likesCount, int commentsCount, boolean isLiked) {}

    public Map<UUID, PostEngagement> queryEngagementByPostIds(List<UUID> postIds, UUID currentUserId) {
        if (postIds == null || postIds.isEmpty()) return Map.of();

        String sql = """
                SELECT
                    p.id AS post_id,
                    COUNT(DISTINCT l.id) AS likes_count,
                    COUNT(DISTINCT c.id) AS comments_count,
                    COALESCE(BOOL_OR(l.user_id = :currentUserId), false) AS is_liked
                FROM posts p
                LEFT JOIN post_likes l ON l.post_id = p.id
                LEFT JOIN comentarios c ON c.post_id = p.id
                WHERE p.id IN (:postIds)
                GROUP BY p.id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("postIds", postIds)
                .addValue("currentUserId", currentUserId);

        Map<UUID, PostEngagement> result = new HashMap<>();
        try {
            jdbcTemplate.query(sql, params, (rs) -> {
                UUID postId = rs.getObject("post_id", UUID.class);
                result.put(postId, new PostEngagement(
                        rs.getInt("likes_count"),
                        rs.getInt("comments_count"),
                        rs.getBoolean("is_liked")));
            });
        } catch (Exception e) {
            log.error("[FeedQueryJdbcAdapter] Error querying engagement: {}", e.getMessage(), e);
        }
        return result;
    }

    public Map<UUID, List<String>> queryTagsByPostIds(List<UUID> postIds) {
        if (postIds == null || postIds.isEmpty()) return Map.of();

        try {
            String sql = "SELECT post_id, LOWER(TRIM(tag)) AS tag FROM post_tags WHERE post_id IN (:postIds)";
            MapSqlParameterSource params = new MapSqlParameterSource().addValue("postIds", postIds);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);

            Map<UUID, List<String>> result = new HashMap<>();
            for (Map<String, Object> row : rows) {
                UUID pId = (UUID) row.get("post_id");
                String tag = (String) row.get("tag");
                if (pId != null && tag != null) {
                    result.computeIfAbsent(pId, k -> new ArrayList<>()).add(tag);
                }
            }
            return result;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    public List<TrendingTopicView> queryTrendingTopics(int limit) {
        String sql = """
                WITH tag_stats AS (
                    SELECT
                        LOWER(TRIM(pt.tag)) AS tag_name,
                        COUNT(DISTINCT p.id) AS posts_using_tag,
                        (COALESCE(SUM(c.comments_count), 0) + COALESCE(SUM(l.likes_count), 0)) AS interaction_volume,
                        MAX(p.created_at) AS latest_created_at
                    FROM post_tags pt
                    JOIN posts p ON p.id = pt.post_id
                    LEFT JOIN (
                        SELECT post_id, COUNT(*) AS comments_count
                        FROM comentarios
                        GROUP BY post_id
                    ) c ON c.post_id = p.id
                    LEFT JOIN (
                        SELECT post_id, COUNT(*) AS likes_count
                        FROM post_likes
                        GROUP BY post_id
                    ) l ON l.post_id = p.id
                    WHERE p.created_at >= :since
                    GROUP BY tag_name
                ),
                latest_posts AS (
                    SELECT DISTINCT ON (ts.tag_name)
                        ts.tag_name,
                        ts.posts_using_tag,
                        ts.interaction_volume,
                        p.id AS latest_post_id,
                        u.id AS autor_id,
                        pf.username AS autor_username,
                        pf.full_name AS autor_full_name,
                        pf.avatar_url AS autor_avatar_url
                    FROM tag_stats ts
                    JOIN posts p ON p.created_at = ts.latest_created_at
                    JOIN post_tags pt ON pt.post_id = p.id AND LOWER(TRIM(pt.tag)) = ts.tag_name
                    JOIN usuarios u ON u.id = p.autor_id
                    LEFT JOIN perfiles pf ON pf.usuario_id = u.id
                    ORDER BY ts.tag_name, p.created_at DESC
                )
                SELECT tag_name, posts_using_tag, interaction_volume,
                       latest_post_id, autor_id, autor_username, autor_full_name, autor_avatar_url
                FROM latest_posts
                ORDER BY (interaction_volume + posts_using_tag * 3) DESC
                LIMIT :limit
                """;

        LocalDateTime since = LocalDateTime.now().minusDays(7);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("since", Timestamp.valueOf(since));

        try {
            return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
                String tagName = rs.getString("tag_name");
                int volume = rs.getInt("interaction_volume") + rs.getInt("posts_using_tag");

                return new TrendingTopicView(
                    rs.getObject("latest_post_id", UUID.class),
                    "#" + tagName,
                    "Tendencia real basada en la actividad reciente.",
                    false,
                    OffsetDateTime.now(),
                    0,
                    0,
                    volume,
                    new FeedAuthorView(
                        rs.getObject("autor_id", UUID.class),
                        rs.getString("autor_username"),
                        rs.getString("autor_full_name"),
                        rs.getString("autor_avatar_url")
                    ),
                    List.of(tagName),
                    null,
                    null
                );
            });
        } catch (Exception e) {
            log.error("[FeedQueryJdbcAdapter] ERROR obtaining dynamic tag trends: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}

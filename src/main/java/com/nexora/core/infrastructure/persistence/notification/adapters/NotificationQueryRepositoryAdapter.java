package com.nexora.core.infrastructure.persistence.notification.adapters;

import com.nexora.core.application.content.dto.FeedAuthorView;
import com.nexora.core.application.content.dto.FeedPostView;
import com.nexora.core.application.notification.ports.NotificationViewRepository;
import com.nexora.core.presentation.graphql.notification.dto.NotificationView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationQueryRepositoryAdapter implements NotificationViewRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<NotificationView> findNotificationHistory(UUID userId, int limit, int offset) {
        if (userId == null) return new ArrayList<>();

        String sql = """
            SELECT
                n.id, n.type, n.content, n.is_read, n.created_at,
                u.id AS sender_id,
                pfs.username AS sender_username,
                pfs.full_name AS sender_full_name,
                pfs.avatar_url AS sender_avatar_url,
                p.id AS post_id,
                p.titulo AS post_titulo,
                p.content AS post_contenido,
                p.is_official AS post_is_official,
                p.image_url AS post_image_url,
                p.created_at AS post_created_at,
                pu.id AS post_author_id,
                pfu.username AS post_author_username,
                pfu.full_name AS post_author_full_name,
                pfu.avatar_url AS post_author_avatar_url
            FROM notifications n
            JOIN usuarios u ON n.sender_id = u.id
            LEFT JOIN perfiles pfs ON pfs.usuario_id = u.id
            LEFT JOIN posts p ON n.post_id = p.id
            LEFT JOIN usuarios pu ON p.autor_id = pu.id
            LEFT JOIN perfiles pfu ON pfu.usuario_id = pu.id
            WHERE n.user_id = :userId
            ORDER BY n.created_at DESC
            LIMIT :limit OFFSET :offset
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("limit", limit)
                .addValue("offset", offset);

        try {
            return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
                Timestamp rawCreatedAt = rs.getTimestamp("created_at");
                OffsetDateTime createdAt = rawCreatedAt != null ?
                    rawCreatedAt.toLocalDateTime().atOffset(ZoneOffset.UTC) : OffsetDateTime.now(ZoneOffset.UTC);

                FeedAuthorView sender = new FeedAuthorView(
                        rs.getObject("sender_id", UUID.class),
                        rs.getString("sender_username"),
                        rs.getString("sender_full_name"),
                        rs.getString("sender_avatar_url"));

                FeedPostView post = null;
                UUID postId = rs.getObject("post_id", UUID.class);
                if (postId != null) {
                    Timestamp rawPostCreatedAt = rs.getTimestamp("post_created_at");
                    OffsetDateTime postCreatedAt = rawPostCreatedAt != null ?
                        rawPostCreatedAt.toLocalDateTime().atOffset(ZoneOffset.UTC) : null;

                    FeedAuthorView postAuthor = new FeedAuthorView(
                            rs.getObject("post_author_id", UUID.class),
                            rs.getString("post_author_username"),
                            rs.getString("post_author_full_name"),
                            rs.getString("post_author_avatar_url"));

                    post = new FeedPostView(
                            postId,
                            rs.getString("post_titulo"),
                            rs.getString("post_contenido"),
                            rs.getBoolean("post_is_official"),
                            postCreatedAt,
                            0, 0, false,
                            postAuthor,
                            new ArrayList<>(),
                            null,
                            rs.getString("post_image_url"));
                }

                return new NotificationView(
                        rs.getObject("id", UUID.class),
                        rs.getString("type"),
                        rs.getString("content"),
                        rs.getBoolean("is_read"),
                        createdAt,
                        sender,
                        post,
                        null
                );
            });
        } catch (Exception e) {
            log.error("[NotificationQueryRepositoryAdapter] Error en history: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public long countUnread(UUID userId) {
        if (userId == null) return 0;

        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = :userId AND is_read = FALSE";
        try {
            return jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("userId", userId), Long.class);
        } catch (Exception e) {
            return 0;
        }
    }
}

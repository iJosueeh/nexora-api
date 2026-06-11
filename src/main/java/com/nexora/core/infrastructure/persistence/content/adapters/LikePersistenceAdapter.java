package com.nexora.core.infrastructure.persistence.content.adapters;

import com.nexora.core.domain.content.repositories.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class LikePersistenceAdapter implements LikeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public boolean existsPostLike(UUID postId, UUID userId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM post_likes WHERE post_id = :postId AND user_id = :userId)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("postId", postId)
                .addValue("userId", userId);
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    @Override
    public void addPostLike(UUID postId, UUID userId) {
        String sql = "INSERT INTO post_likes (post_id, user_id) VALUES (:postId, :userId)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("postId", postId)
                .addValue("userId", userId);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public void removePostLike(UUID postId, UUID userId) {
        String sql = "DELETE FROM post_likes WHERE post_id = :postId AND user_id = :userId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("postId", postId)
                .addValue("userId", userId);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public boolean existsCommentLike(UUID commentId, UUID userId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM comment_likes WHERE comment_id = :commentId AND user_id = :userId)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("commentId", commentId)
                .addValue("userId", userId);
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    @Override
    public void addCommentLike(UUID commentId, UUID userId) {
        String sql = "INSERT INTO comment_likes (comment_id, user_id) VALUES (:commentId, :userId)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("commentId", commentId)
                .addValue("userId", userId);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public void removeCommentLike(UUID commentId, UUID userId) {
        String sql = "DELETE FROM comment_likes WHERE comment_id = :commentId AND user_id = :userId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("commentId", commentId)
                .addValue("userId", userId);
        jdbcTemplate.update(sql, params);
    }
}

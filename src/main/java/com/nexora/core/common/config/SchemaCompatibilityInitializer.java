package com.nexora.core.common.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Applies idempotent schema updates required by recent feed features.
 * This avoids runtime failures when environments still have older schemas.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SchemaCompatibilityInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        ensurePostsLocationColumn();
        ensurePostTagsTable();
        ensurePostTagsTagColumn();
        ensureCommentsTable();
        ensureCommentLikesTable();
    }

    private void ensureCommentLikesTable() {
        try {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS comment_likes (
                        comment_id UUID NOT NULL,
                        user_id UUID NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (comment_id, user_id),
                        CONSTRAINT fk_comment_likes_comment FOREIGN KEY (comment_id) REFERENCES comentarios(id) ON DELETE CASCADE,
                        CONSTRAINT fk_comment_likes_user FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE CASCADE
                    )
                    """);
            log.info("Schema compatibility: confirmed 'comment_likes' table exists.");
        } catch (Exception ex) {
            log.warn("Schema compatibility: could not ensure 'comment_likes' table", ex);
        }
    }

    private void ensureCommentsTable() {
        try {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS comentarios (
                        id UUID PRIMARY KEY,
                        post_id UUID NOT NULL,
                        parent_id UUID,
                        autor_id UUID NOT NULL,
                        content TEXT NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
                        CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES comentarios(id) ON DELETE CASCADE,
                        CONSTRAINT fk_comment_author FOREIGN KEY (autor_id) REFERENCES usuarios(id) ON DELETE CASCADE
                    )
                    """);
            log.info("Schema compatibility: confirmed 'comentarios' table exists.");
        } catch (Exception ex) {
            log.warn("Schema compatibility: could not ensure 'comentarios' table", ex);
        }
    }

    private void ensurePostsLocationColumn() {
        try {
            jdbcTemplate.execute("ALTER TABLE posts ADD COLUMN IF NOT EXISTS location VARCHAR(120)");
        } catch (Exception ex) {
            log.warn("Schema compatibility: could not ensure posts.location column", ex);
        }
    }

    private void ensurePostTagsTable() {
        try {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS post_tags (
                        post_id UUID NOT NULL,
                        tag VARCHAR(100) NOT NULL,
                        CONSTRAINT fk_post_tags_post
                            FOREIGN KEY (post_id) REFERENCES posts(id)
                            ON DELETE CASCADE
                    )
                    """);
        } catch (Exception ex) {
            log.warn("Schema compatibility: could not ensure post_tags table", ex);
        }
    }

    private void ensurePostTagsTagColumn() {
        try {
            Boolean hasTagColumn = jdbcTemplate.queryForObject("""
                    SELECT EXISTS (
                        SELECT 1
                        FROM information_schema.columns
                        WHERE table_name = 'post_tags'
                          AND column_name = 'tag'
                    )
                    """, Boolean.class);

            if (Boolean.TRUE.equals(hasTagColumn)) {
                return;
            }

            jdbcTemplate.execute("ALTER TABLE post_tags ADD COLUMN IF NOT EXISTS tag VARCHAR(100)");

            String sourceColumn = resolveLegacyTagColumn();
            if (sourceColumn != null) {
                jdbcTemplate.execute("""
                        UPDATE post_tags
                        SET tag = %s
                        WHERE tag IS NULL OR TRIM(tag) = ''
                        """.formatted(sourceColumn));
            }

            jdbcTemplate.execute("ALTER TABLE post_tags ALTER COLUMN tag SET NOT NULL");
        } catch (Exception ex) {
            log.warn("Schema compatibility: could not ensure post_tags.tag column", ex);
        }
    }

    private String resolveLegacyTagColumn() {
        String[] candidates = {"tag_name", "tags", "name"};

        for (String candidate : candidates) {
            try {
                Boolean exists = jdbcTemplate.queryForObject("""
                        SELECT EXISTS (
                            SELECT 1
                            FROM information_schema.columns
                            WHERE table_name = 'post_tags'
                              AND column_name = '%s'
                        )
                        """.formatted(candidate), Boolean.class);

                if (Boolean.TRUE.equals(exists)) {
                    return candidate;
                }
            } catch (Exception ignored) {
                // If metadata lookup fails for one candidate, keep trying the rest.
            }
        }

        return null;
    }
}

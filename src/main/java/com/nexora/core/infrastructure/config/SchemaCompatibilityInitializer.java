package com.nexora.core.infrastructure.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchemaCompatibilityInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Iniciando verificacion de compatibilidad de esquema...");
        ensurePostsLocationColumn();
        ensurePostTagsTable();
        ensurePostTagsTagColumn();
        ensureCommentsTable();
        ensureCommentLikesTable();
        log.info("Verificacion de compatibilidad de esquema completada.");
    }

    private void ensurePostsLocationColumn() {
        try {
            jdbcTemplate.execute("ALTER TABLE posts ADD COLUMN IF NOT EXISTS location VARCHAR(255)");
        } catch (Exception e) {
            log.warn("No se pudo agregar columna location a posts: {}", e.getMessage());
        }
    }

    private void ensurePostTagsTable() {
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS post_tags (
                    post_id UUID NOT NULL,
                    tag VARCHAR(100) NOT NULL,
                    PRIMARY KEY (post_id, tag)
                )
            """);
        } catch (Exception e) {
            log.warn("No se pudo crear tabla post_tags: {}", e.getMessage());
        }
    }

    private void ensurePostTagsTagColumn() {
        try {
            jdbcTemplate.execute("ALTER TABLE post_tags ADD COLUMN IF NOT EXISTS tag VARCHAR(100)");
        } catch (Exception e) {
            log.warn("No se pudo agregar columna tag a post_tags: {}", e.getMessage());
        }
    }

    private void ensureCommentsTable() {
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS comentarios (
                    id UUID PRIMARY KEY,
                    post_id UUID NOT NULL REFERENCES posts(id),
                    usuario_id UUID NOT NULL,
                    content TEXT NOT NULL,
                    likes_count INT DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
        } catch (Exception e) {
            log.warn("No se pudo crear tabla comentarios: {}", e.getMessage());
        }
    }

    private void ensureCommentLikesTable() {
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS comment_likes (
                    comment_id UUID NOT NULL,
                    usuario_id UUID NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (comment_id, usuario_id)
                )
            """);
        } catch (Exception e) {
            log.warn("No se pudo crear tabla comment_likes: {}", e.getMessage());
        }
    }
}

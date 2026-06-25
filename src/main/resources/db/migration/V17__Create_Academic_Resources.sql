-- V17__Create_Academic_Resources.sql

-- Categorías por carrera
CREATE TABLE resource_categories (
    id UUID PRIMARY KEY,
    carrera_id UUID NOT NULL REFERENCES carreras(id),
    name VARCHAR(150) NOT NULL,
    CONSTRAINT uq_resource_category_per_career UNIQUE (carrera_id, name)
);

-- Recursos académicos
CREATE TABLE academic_resources (
    id UUID PRIMARY KEY,
    slug VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    type VARCHAR(20) NOT NULL,
    category_id UUID NOT NULL REFERENCES resource_categories(id),
    author_id UUID NOT NULL REFERENCES usuarios(id),
    file_url VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    file_format VARCHAR(10) NOT NULL,
    average_rating DECIMAL(3,2) DEFAULT 0,
    ratings_count INTEGER DEFAULT 0,
    download_count INTEGER DEFAULT 0,
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Calificaciones
CREATE TABLE resource_ratings (
    id UUID PRIMARY KEY,
    resource_id UUID NOT NULL REFERENCES academic_resources(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    rating SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_resource_rating UNIQUE (user_id, resource_id)
);

-- Índices para optimizar queries de la Fase 3
CREATE INDEX idx_academic_resources_category ON academic_resources(category_id);
CREATE INDEX idx_academic_resources_author ON academic_resources(author_id);
CREATE INDEX idx_academic_resources_deleted ON academic_resources(deleted_at) WHERE deleted_at IS NULL;
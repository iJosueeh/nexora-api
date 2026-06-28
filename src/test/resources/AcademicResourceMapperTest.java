package com.nexora.core.infrastructure.persistence.content.mappers;

import com.nexora.core.domain.content.aggregates.AcademicResource;
import com.nexora.core.infrastructure.persistence.content.entities.AcademicResourceJpaEntity;
import com.nexora.core.infrastructure.persistence.content.entities.ResourceCategoryJpaEntity;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AcademicResourceMapperTest {

    @Test
    void shouldMapEntityToDomain() {
        // Arrange (Preparar datos)
        UUID entityId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        ResourceCategoryJpaEntity mockCategory = new ResourceCategoryJpaEntity();
        mockCategory.setId(categoryId);

        AcademicResourceJpaEntity entity = AcademicResourceJpaEntity.builder()
                .slug("guia-calculo-1")
                .title("Guía de Cálculo 1")
                .description("Ejercicios resueltos")
                .type("GUIDE")
                .category(mockCategory)
                .authorId(authorId)
                .fileUrl("path/to/file.pdf")
                .fileSize(1024L)
                .fileFormat("PDF")
                .build();
        entity.setId(entityId); // Seteamos el ID heredado

        // Act (Ejecutar)
        AcademicResource domain = AcademicResourceMapper.toDomain(entity);

        // Assert (Verificar)
        assertNotNull(domain);
        assertEquals(entityId, domain.getId());
        assertEquals(categoryId, domain.getCategoryId());
        assertEquals("guia-calculo-1", domain.getSlug());
        assertEquals(authorId, domain.getAuthorId());
        assertEquals("PDF", domain.getFileFormat());
    }
}
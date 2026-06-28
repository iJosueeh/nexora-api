# Requerimientos: Repositorio de Recursos Académicos

## 1. Visión General

API GraphQL para el repositorio de recursos académicos: subida, categorización por carrera, calificación (1-5), descarga con URLs prefirmadas y gestión de autoría.

---

## 2. Requerimientos Funcionales

| ID | Requerimiento | Prioridad | Estimación |
|----|--------------|-----------|------------|
| RF-RES-01 | Exponer query `resources(cursor, limit, filter)` con filtros y paginación | Alta | 8 pts |
| RF-RES-02 | Exponer query `resourceById(id)` para detalle | Alta | 3 pts |
| RF-RES-03 | Exponer mutation `uploadResource(input, file)` con multipart | Alta | 10 pts |
| RF-RES-04 | Exponer mutation `updateResource(id, input)` para metadatos | Alta | 5 pts |
| RF-RES-05 | Exponer mutation `deleteResource(id)` (soft delete) | Alta | 3 pts |
| RF-RES-06 | Exponer mutation `rateResource(resourceId, rating)` con upsert | Alta | 5 pts |
| RF-RES-07 | Exponer query `myResources` para recursos del usuario autenticado | Media | 5 pts |
| RF-RES-08 | Exponer query `resourceCategories(careerId)` para catálogo | Alta | 3 pts |
| RF-RES-09 | Validar formato y tamaño de archivo (20MB max) | Alta | 3 pts |
| RF-RES-10 | Generar URL prefirmada para descarga (15 min exp) | Alta | 5 pts |
| RF-RES-11 | Validar categoría contra catálogo institucional | Alta | 3 pts |
| RF-RES-12 | Recalcular averageRating al recibir nuevo voto | Alta | 5 pts |

**Total estimado:** 58 pts

---

## 3. Requerimientos No Funcionales

| ID | Requerimiento | Tipo | Métrica |
|----|--------------|------|---------|
| RNF-RES-01 | Upload < 5s para < 10MB | Rendimiento | 5s |
| RNF-RES-02 | Consulta con filtros < 500ms | Rendimiento | 500ms |
| RNF-RES-03 | Archivos en S3, bucket separado `nexora-resources` | Almacenamiento | — |
| RNF-RES-04 | URLs prefirmadas expiran en 15 min | Seguridad | 15 min |
| RNF-RES-05 | Rating upsert atómico (no race conditions) | Consistencia | — |
| RNF-RES-06 | Soft delete: `deleted_at` not null | Integridad | — |

---

## 4. Reglas de Negocio

| ID | Regla | Excepción |
|----|-------|-----------|
| RN-RES-01 | Archivo max 20MB | `FileTooLargeException` |
| RN-RES-02 | Formatos: PDF, EPUB, MD, PPTX, DOCX | `InvalidFileFormatException` |
| RN-RES-03 | Rating 1-5 | `ValidationException` |
| RN-RES-04 | Rating único por (usuario, recurso) | Unique constraint |
| RN-RES-05 | Archivo inmutable post-creación | — |
| RN-RES-06 | Solo autor edita/elimina | `AccessDeniedException` |

---

## 5. Casos de Uso

### CU-RES-01: Upload resource
- **Mutation:** `uploadResource(input: ResourceInput!, file: Upload!)`
- **Input:** title, description?, categoryId, type
- **Validaciones:** title 5-200, file ≤ 20MB, formato válido, categoría existe
- **Proceso:** Subir a S3 → persistir entidad → retornar `AcademicResource`
- **Postcondición:** Archivo en S3, registro en BD

### CU-RES-02: Rate resource (upsert)
- **Mutation:** `rateResource(resourceId, rating)`
- **Lógica:** Busca `ResourceRating` por `(userId, resourceId)`. Si existe → actualiza. Si no → inserta.
- **Postcondición:** `ResourceRating` actualizado, `AcademicResource.averageRating` recalculado

---

## 6. Criterios de Aceptación

### CA-RES-01: Upload con validación
```
Given un estudiante autenticado
When sube un archivo .exe de 1MB
Then el sistema retorna "Invalid file format. Allowed: PDF, EPUB, MD, PPTX, DOCX"
```

### CA-RES-02: Rating upsert atómico
```
Given 2 usuarios votan simultáneamente el mismo recurso
When ambos ratings se procesan
Then ratingsCount = 2 (sin race condition)
And averageRating calculado correctamente
```

---

## 7. Matriz de Trazabilidad

| RF | Resolver | Service | Repository | Entity |
|----|---------|---------|------------|--------|
| RF-RES-01 | `ResourceQueryResolver` | `ResourceService` | `AcademicResourceRepository` | `AcademicResource` |
| RF-RES-03 | `ResourceMutationResolver` | `ResourceService` | `AcademicResourceRepository` | `AcademicResource` |
| RF-RES-06 | `ResourceMutationResolver` | `RatingService` | `ResourceRatingRepository` | `ResourceRating` |
| RF-RES-08 | `ResourceQueryResolver` | `CatalogService` | `ResourceCategoryRepository` | `ResourceCategory` |
| RF-RES-10 | — | `FileStorageService` | — | — |

---

## 8. Dependencias

| Dependencia | Tipo |
|-------------|------|
| `AcademicResource`, `ResourceCategory`, `ResourceRating` | Entidades |
| `AcademicResourceRepository` | Repositorio |
| `FileStorageService` | Servicio |
| S3-compatible storage (bkt `nexora-resources`) | Infraestructura |
| `CatalogRepository` | Repositorio |

---

## 9. Priorización

### MVP
RF-RES-01, RF-RES-02, RF-RES-03, RF-RES-06, RF-RES-07, RF-RES-09, RF-RES-10, RF-RES-11, RF-RES-12

### Fase 2
RF-RES-04, RF-RES-05, RF-RES-08

---

## 10. TODOs

| ID | Tarea | Prioridad | Esfuerzo |
|----|-------|-----------|----------|
| TODO-RES-01 | Migración Flyway: academic_resources + resource_ratings + resource_categories | Alta | 4h |
| TODO-RES-02 | Integrar multipart upload en GraphQL con Apollo | Alta | 6h |
| TODO-RES-03 | Validación de tipo MIME real (no solo extensión) | Alta | 3h |
| TODO-RES-04 | Recalcular averageRating con query atómica (UPDATE ... SET avg = (SELECT AVG...)) | Alta | 4h |
| TODO-RES-05 | Generación de URLs prefirmadas con expiración de 15 min | Alta | 5h |
| TODO-RES-06 | Tests de integración: upload file → rate → query filters | Alta | 10h |
| TODO-RES-07 | Tests unitarios: validación formato, tamaño, upsert rating | Alta | 6h |
| TODO-RES-08 | Búsqueda full-text con tsvector | Media | 8h |
| TODO-RES-09 | Seed data de ResourceCategory desde InstitutionalCatalog | Media | 3h |
| TODO-RES-10 | Reportar recurso inapropiado (integración con Management) | Baja | 4h |

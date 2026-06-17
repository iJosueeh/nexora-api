# Módulo: Repositorio de Recursos Académicos

## 1. Objetivo del módulo

Espacio dedicado para que los estudiantes compartan, categoricen por carrera y califiquen materiales de estudio de autoría propia como resúmenes, guías, apuntes y flashcards.

## 2. Alcance

- **Incluye:** CRUD de recursos académicos, categorización por carrera, sistema de calificación (1-5), descarga con autenticación.
- **Incluye:** Búsqueda y filtrado por carrera, tipo de recurso, rating.
- **No incluye:** Comentarios por recurso, detección de plagio, vista previa de PDF.

## 3. Lógica de negocio

- **Subida de recursos:**
  - Solo estudiantes autenticados pueden subir.
  - El recurso debe categorizarse con una carrera del catálogo institucional.
  - El archivo se almacena en storage externo (S3-compatible), en BD solo la URL y metadatos.
  - El autor puede actualizar metadatos pero no reemplazar el archivo.
- **Calificación:**
  - Un estudiante puede calificar una vez por recurso (1-5 estrellas).
  - Si ya votó, la nueva calificación reemplaza la anterior.
  - El rating promedio se calcula y cachea en la entidad `AcademicResource`.
- **Descarga:**
  - Requiere autenticación para trazabilidad.
  - Se genera URL prefirmada con expiración (15 minutos) para proteger el storage.

## 4. Validaciones

### Entrada

- `ResourceInput.title`: requerido, min 5, max 200.
- `ResourceInput.description`: opcional, max 2000.
- `ResourceInput.categoryId`: UUID, debe existir en catálogo.
- `ResourceInput.type`: enum `SUMMARY | GUIDE | FLASHCARD | EXAM | OTHER`.
- Archivo: max 20MB, formatos `PDF, EPUB, MD, PPTX, DOCX`.

### Reglas de dominio

- Un usuario solo puede calificar un recurso una vez (upsert).
- El archivo no puede reemplazarse después de publicado.
- Soft delete: el recurso se oculta pero el archivo persiste (referencia para reportes).

## 5. Contratos API

### GraphQL

```graphql
type Query {
    resources(cursor: String, limit: Int, filter: ResourceFilter): ResourceConnection!
    resourceById(id: UUID!): AcademicResource
    myResources: [AcademicResource!]!
    resourceCategories(careerId: UUID): [ResourceCategory!]!
}

type Mutation {
    uploadResource(input: ResourceInput!, file: Upload!): AcademicResource!
    updateResource(id: UUID!, input: ResourceInput!): AcademicResource!
    deleteResource(id: UUID!): Boolean!
    rateResource(resourceId: UUID!, rating: Int!): ResourceRatingPayload!
}

type AcademicResource {
    id: UUID!
    title: String!
    description: String
    type: ResourceType!
    category: ResourceCategory!
    author: User!
    fileUrl: String!
    fileSize: Long!
    fileFormat: String!
    averageRating: Float!
    ratingsCount: Int!
    userRating: Int
    downloadCount: Int!
    createdAt: DateTime!
    updatedAt: DateTime
}

enum ResourceType { SUMMARY GUIDE FLASHCARD EXAM OTHER }
```

### REST

- No aplica (GraphQL + multipart upload manejado por Apollo).

## 6. Persistencia

- Entidades: `AcademicResource`, `ResourceCategory`, `ResourceRating`.
- Repositorios: `AcademicResourceRepository`, `ResourceCategoryRepository`, `ResourceRatingRepository`.
- `AcademicResource` → `ResourceCategory` (M:1), `User` (author M:1).
- `ResourceRating`: unique constraint `(user_id, resource_id)`.
- Índices: `(category_id, average_rating DESC)`, `(author_id)`.

## 7. Seguridad

- `resources`, `resourceById`, `resourceCategories` — público.
- `uploadResource`, `updateResource`, `deleteResource` — autenticado, ownership validado.
- `rateResource` — autenticado.
- URLs prefirmadas expiran en 15 minutos.

## 8. Errores y excepciones

| Escenario | Excepción | Código GraphQL |
|-----------|-----------|----------------|
| Archivo excede tamaño | `FileTooLargeException` | BAD_REQUEST |
| Formato no soportado | `InvalidFileFormatException` | BAD_REQUEST |
| Categoría no encontrada | `ResourceNotFoundException` | NOT_FOUND |
| Rating fuera de rango | `ValidationException` | BAD_REQUEST |
| No autorizado a editar | `AccessDeniedException` | FORBIDDEN |

## 9. Dependencias del módulo

- `FileStorageService` — almacenamiento S3-compatible
- `ResourceCategoryRepository` — catálogo de categorías
- `UserRepository` — validación de autor
- `CatalogService` — validación de carrera

## 10. Observabilidad

- Métrica: recursos subidos por día, por categoría.
- Contador de descargas (incremento atómico en BD).
- Loggear subidas y eliminaciones (userId, resourceId).

## 11. Casos de prueba sugeridos

- Subida exitosa de PDF retorna recurso con URL.
- Rating upsert: votar dos veces actualiza el voto anterior.
- Descarga genera URL prefirmada válida.
- Archivo > 20MB es rechazado.
- Recurso de otro usuario no puede editarse.

## 12. TODO / Pendientes

- Vista previa de PDF en navegador (PDF.js).
- Comentarios/discusión por recurso.
- Reportar recurso inapropiado (moderación).
- Sistema de colecciones de recursos.
- Indexación para búsqueda全文 (full-text search).

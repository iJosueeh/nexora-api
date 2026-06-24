# Módulo: Marcadores y Colecciones Guardadas

## 1. Objetivo del módulo

Permitir que cada estudiante guarde publicaciones, hilos de discusión y recursos académicos en carpetas privadas organizadas para su revisión posterior.

## 2. Alcance

- **Incluye:** CRUD de colecciones, guardar/remover publicaciones en colecciones, consulta de items por colección.
- **Incluye:** Límites por usuario, colecciones privadas por defecto.
- **No incluye:** Colecciones compartidas, exportación, etiquetado interno.

## 3. Lógica de negocio

- **Colecciones:**
  - Cada estudiante puede crear hasta 20 colecciones.
  - Nombre único por usuario (no global).
  - Solo visibles para el dueño (privadas).
  - Eliminar una colección no afecta las publicaciones originales (soft delete lógico de la relación).
- **Items:**
  - Una publicación puede estar en múltiples colecciones del mismo usuario.
  - Máximo 200 items por colección.
  - Si la publicación original se elimina, el item muestra "contenido no disponible" pero no se remueve automáticamente.
- **Tipos soportados:** `POST`, `RESOURCE`, `EVENT`.

## 4. Validaciones

### Entrada

- `CollectionInput.name`: requerido, min 3, max 100, único por usuario.
- `CollectionInput.description`: opcional, max 500.
- `SaveItemInput.collectionId`: UUID, debe existir y pertenecer al usuario.
- `SaveItemInput.itemId`: UUID, debe existir (post/resource/event).
- `SaveItemInput.itemType`: enum `POST | RESOURCE | EVENT`.

### Reglas de dominio

- Máximo 20 colecciones por usuario.
- Máximo 200 items por colección.
- Un item no puede guardarse duplicado en la misma colección (unique constraint).
- Solo el dueño puede modificar sus colecciones.

## 5. Contratos API

### GraphQL

```graphql
type Query {
    myCollections: [Collection!]!
    collectionItems(slug: String!, cursor: String, limit: Int): CollectionItemConnection!
}

type Mutation {
    createCollection(input: CollectionInput!): Collection!
    updateCollection(id: UUID!, input: CollectionInput!): Collection!
    deleteCollection(id: UUID!): Boolean!
    saveItemToCollection(input: SaveItemInput!): CollectionItem!
    removeItemFromCollection(itemId: UUID!): Boolean!
    reorderCollectionItems(collectionId: UUID!, orderedIds: [UUID!]!): Boolean!
}

type Collection {
    id: UUID!
    name: String!
    slug: String!
    description: String
    itemCount: Int!
    items: [CollectionItem!]
    createdAt: DateTime!
    updatedAt: DateTime
}

type CollectionItem {
    id: UUID!
    collectionId: UUID!
    itemId: UUID!
    itemType: BookmarkItemType!
    order: Int!
    savedAt: DateTime!
    item: BookmarkableItem
}

union BookmarkableItem = Post | AcademicResource | Event
enum BookmarkItemType { POST RESOURCE EVENT }
```

### REST

- No aplica.

## 6. Persistencia

- Entidades: `Collection`, `CollectionItem`.
- Repositorios: `CollectionRepository`, `CollectionItemRepository`.
- `Collection` → `User` (M:1), `CollectionItem` → `Collection` (M:1).
- Unique constraints: `(user_id, name)` en Collection, `(collection_id, item_id, item_type)` en CollectionItem.
- Slug generado automáticamente a partir del nombre (único por usuario).

## 7. Seguridad

- Todos los endpoints requieren autenticación.
- Validación de ownership: `CollectionRepository.findByUserIdAndId(userId, id)`.
- `updateCollection`, `deleteCollection` — solo OWNER.

## 8. Errores y excepciones

| Escenario | Excepción | Código GraphQL |
|-----------|-----------|----------------|
| Límite de colecciones alcanzado | `MaxCollectionsLimitException` | BAD_REQUEST |
| Colección llena | `CollectionFullException` | BAD_REQUEST |
| Nombre duplicado (mismo usuario) | `DuplicateCollectionNameException` | BAD_REQUEST |
| Colección no encontrada | `ResourceNotFoundException` | NOT_FOUND |
| Item duplicado en colección | — | OK (idempotente, retorna existente) |

## 9. Dependencias del módulo

- `CollectionRepository`, `CollectionItemRepository`
- `PostRepository`, `AcademicResourceRepository`, `EventRepository` — validación de items existentes
- `SlugService` — generación de slugs

## 10. Observabilidad

- Métrica: promedio de colecciones por usuario, items por colección.
- Loggear creación/eliminación de colecciones.
- Métrica: tipos de items más guardados (POST vs RESOURCE vs EVENT).

## 11. Casos de prueba sugeridos

- Crear colección con nombre válido.
- Guardar publicación en colección.
- Intentar guardar duplicado en misma colección → retorna item existente.
- Colección llena (200 items) → error.
- Eliminar colección no elimina las publicaciones.
- Usuario no puede ver colecciones de otro usuario.

## 12. TODO / Pendientes

- Colecciones compartidas (colaborativas con permisos de lectura/escritura).
- Exportar colección como lista JSON/CSV.
- Búsqueda dentro de colecciones.
- Sugerir colecciones basadas en contenido similar.

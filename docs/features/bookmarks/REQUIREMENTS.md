# Requerimientos: Marcadores y Colecciones Guardadas

## 1. Visión General

API GraphQL que permite a los estudiantes organizar publicaciones, recursos y eventos en colecciones privadas. Soporta CRUD de colecciones, items polimórficos y límites de almacenamiento por usuario.

---

## 2. Requerimientos Funcionales

| ID | Requerimiento | Prioridad | Estimación |
|----|--------------|-----------|------------|
| RF-BMK-01 | Exponer mutation `createCollection(input)` para crear colección | Alta | 5 pts |
| RF-BMK-02 | Exponer mutation `updateCollection(id, input)` para editar | Alta | 3 pts |
| RF-BMK-03 | Exponer mutation `deleteCollection(id)` para eliminar (sin cascade) | Alta | 3 pts |
| RF-BMK-04 | Exponer mutation `saveItemToCollection(input)` para guardar items | Alta | 8 pts |
| RF-BMK-05 | Exponer mutation `removeItemFromCollection(itemId)` | Alta | 3 pts |
| RF-BMK-06 | Exponer mutation `reorderCollectionItems(collectionId, orderedIds)` | Media | 5 pts |
| RF-BMK-07 | Exponer query `myCollections` para listar colecciones del usuario | Alta | 5 pts |
| RF-BMK-08 | Exponer query `collectionItems(slug, cursor, limit)` para contenido | Alta | 5 pts |
| RF-BMK-09 | Validar nombre único por usuario | Alta | 3 pts |
| RF-BMK-10 | Validar límite de 20 colecciones por usuario | Alta | 3 pts |
| RF-BMK-11 | Validar límite de 200 items por colección | Alta | 3 pts |
| RF-BMK-12 | Implementar unique constraint (collection_id, item_id, item_type) | Alta | 3 pts |
| RF-BMK-13 | Soportar tipos polimórficos: POST, RESOURCE, EVENT | Alta | 5 pts |

**Total estimado:** 54 pts

---

## 3. Requerimientos No Funcionales

| ID | Requerimiento | Tipo | Métrica |
|----|--------------|------|---------|
| RNF-BMK-01 | Query `myCollections` < 300ms | Rendimiento | 300ms |
| RNF-BMK-02 | Mutation `saveItem` < 500ms | Rendimiento | 500ms |
| RNF-BMK-03 | Colecciones privadas (solo owner) | Seguridad | — |
| RNF-BMK-04 | Item duplicado = idempotente | Consistencia | — |
| RNF-BMK-05 | Colección eliminada no afecta originales | Integridad | — |

---

## 4. Reglas de Negocio

| ID | Regla | Excepción |
|----|-------|-----------|
| RN-BMK-01 | Nombre único por usuario | `DuplicateCollectionNameException` |
| RN-BMK-02 | Límite: 20 colecciones | `MaxCollectionsLimitException` |
| RN-BMK-03 | Límite: 200 items/colección | `CollectionFullException` |
| RN-BMK-04 | Unique (collection_id, item_id, item_type) | Unique constraint |
| RN-BMK-05 | Items polimórficos: POST, RESOURCE, EVENT | Union type GraphQL |

---

## 5. Casos de Uso

### CU-BMK-01: Guardar item con verificación de límites
1. Mutation `saveItemToCollection({ collectionId, itemId, itemType })`
2. Sistema verifica que la colección pertenezca al usuario
3. Sistema busca duplicado: si existe, retorna existente
4. Sistema verifica itemCount < 200
5. Sistema persiste `CollectionItem`
6. Sistema incrementa `itemCount` en `Collection`
7. Retorna `CollectionItem`

### CU-BMK-02: Eliminar colección (sin cascade)
1. Mutation `deleteCollection(id)`
2. Sistema verifica ownership
3. Sistema elimina `Collection` (soft delete o físico)
4. Sistema elimina todos `CollectionItem` asociados
5. Items originales (posts, resources, events) no se modifican

---

## 6. Criterios de Aceptación

### CA-BMK-01: Nombre único por usuario
```
Given Usuario A tiene colección "Favoritos"
When Usuario A crea otra colección "Favoritos"
Then error "Collection name already exists"
Given Usuario B crea colección "Favoritos"
Then éxito (nombre único por usuario, no global)
```

### CA-BMK-02: Items polimórficos
```
Given una colección existente
When se guarda un Post (itemType=POST, itemId=post-uuid)
And se guarda un Resource (itemType=RESOURCE, itemId=resource-uuid)
Then collectionItems retorna ambos con itemType diferenciado
And el union type BookmarkableItem resuelve correctamente
```

---

## 7. Matriz de Trazabilidad

| RF | Resolver | Service | Repository | Entity |
|----|---------|---------|------------|--------|
| RF-BMK-01 | `BookmarkMutationResolver` | `BookmarkService` | `CollectionRepository` | `Collection` |
| RF-BMK-04 | `BookmarkMutationResolver` | `BookmarkService` | `CollectionItemRepository` | `CollectionItem` |
| RF-BMK-07 | `BookmarkQueryResolver` | `BookmarkService` | `CollectionRepository` | `Collection` |
| RF-BMK-08 | `BookmarkQueryResolver` | `BookmarkService` | `CollectionItemRepository` | `CollectionItem` |
| RF-BMK-13 | `BookmarkQueryResolver` | — | — | `BookmarkableItem` (union) |

---

## 8. Dependencias

| Dependencia | Tipo |
|-------------|------|
| `Collection`, `CollectionItem` | Entidades |
| `CollectionRepository`, `CollectionItemRepository` | Repositorios |
| `Post`, `AcademicResource`, `Event` | Entidades (referencia) |
| `SlugService` | Servicio |

---

## 9. Priorización

### MVP
RF-BMK-01, RF-BMK-02, RF-BMK-03, RF-BMK-04, RF-BMK-05, RF-BMK-07, RF-BMK-08, RF-BMK-09, RF-BMK-10, RF-BMK-11, RF-BMK-12

### Fase 2
RF-BMK-06, RF-BMK-13

---

## 10. TODOs

| ID | Tarea | Prioridad | Esfuerzo | Notas |
|----|-------|-----------|----------|-------|
| TODO-BMK-01 | Migración Flyway: collections + collection_items | Alta | 4h | V5__create_bookmarks.sql |
| TODO-BMK-02 | Implementar slug único por usuario para colecciones | Alta | 3h | SlugService |
| TODO-BMK-03 | Unique constraints en BD: (user_id, name), (collection_id, item_id, item_type) | Alta | 2h | DDL |
| TODO-BMK-04 | Validación de límites en servicio (20 colecciones, 200 items) | Alta | 4h | COUNT queries |
| TODO-BMK-05 | Validación de ownership en todas las mutations | Alta | 3h | SecurityUtil |
| TODO-BMK-06 | Tests de integración: CRUD colecciones, guardar items, límites | Alta | 10h | GraphQLTestTemplate |
| TODO-BMK-07 | Tests unitarios: validaciones, límites, duplicados | Alta | 6h | Mockito |
| TODO-BMK-08 | Implementar Union type BookmarkableItem en schema.graphqls | Alta | 3h | type BookmarkableItem = Post | AcademicResource | Event |
| TODO-BMK-09 | Colecciones compartidas (colaborativas) | Baja | 12h | Permisos de lectura/escritura |
| TODO-BMK-10 | Búsqueda dentro de colecciones | Media | 6h | LIKE o tsvector |

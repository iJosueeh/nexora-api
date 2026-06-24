# Requerimientos: Sistema de Comentarios Anidados

## 1. Visión General

API GraphQL para comentarios con estructura jerárquica. El servidor retorna los comentarios como lista plana con `parentId` y el cliente construye el árbol. Anidación máxima de 3 niveles, soft delete con preservación de estructura.

---

## 2. Requerimientos Funcionales

| ID | Requerimiento | Prioridad | Estimación |
|----|--------------|-----------|------------|
| RF-COM-01 | Exponer query `commentsByPost(postId, cursor, limit)` con paginación | Alta | 8 pts |
| RF-COM-02 | Exponer mutation `createComment(input)` para crear comentarios | Alta | 8 pts |
| RF-COM-03 | Exponer mutation `updateComment(id, content)` para editar | Alta | 5 pts |
| RF-COM-04 | Exponer mutation `deleteComment(id)` para soft delete | Alta | 5 pts |
| RF-COM-05 | Exponer mutation `likeComment(commentId)` con toggle | Alta | 5 pts |
| RF-COM-06 | Validar profundidad máxima de anidación (≤ 3 niveles) | Alta | 5 pts |
| RF-COM-07 | Validar contenido entre 1 y 1000 caracteres | Alta | 2 pts |
| RF-COM-08 | Implementar soft delete con preservación de hijos | Alta | 5 pts |
| RF-COM-09 | Implementar unique constraint `(user_id, comment_id)` para likes | Alta | 3 pts |
| RF-COM-10 | Validar ventana de edición de 1 hora | Alta | 3 pts |
| RF-COM-11 | Retornar lista plana (no árbol) para que el cliente construya jerarquía | Alta | 2 pts |

**Total estimado:** 51 pts

---

## 3. Requerimientos No Funcionales

| ID | Requerimiento | Tipo | Métrica |
|----|--------------|------|---------|
| RNF-COM-01 | Query `commentsByPost` < 300ms para 100 comentarios | Rendimiento | 300ms |
| RNF-COM-02 | Mutation `createComment` < 500ms | Rendimiento | 500ms |
| RNF-COM-03 | Like idempotente (misma respuesta si ya existe) | Consistencia | — |
| RNF-COM-04 | Soft delete no elimina físicamente el registro | Integridad | — |
| RNF-COM-05 | Índice compuesto en `(post_id, parent_id, created_at)` | Rendimiento | — |

---

## 4. Reglas de Negocio

| ID | Regla | Excepción |
|----|-------|-----------|
| RN-COM-01 | Profundidad máxima 3 niveles | `MaxDepthExceededException` |
| RN-COM-02 | Contenido 1-1000 caracteres | `ValidationException` |
| RN-COM-03 | Edición permitida solo 1h post-creación | `EditWindowExpiredException` |
| RN-COM-04 | Like único por (usuario, comentario) | Unique constraint |
| RN-COM-05 | Soft delete: `is_deleted = true`, content = NULL | — |
| RN-COM-06 | Solo el autor puede editar/eliminar | `AccessDeniedException` |

---

## 5. Casos de Uso

### CU-COM-01: Crear comentario con validación de profundidad
- **Input:** `CommentInput { postId, content, parentId? }`
- **Validación profundidad:** Si `parentId` existe, calcular profundidad del padre y validar ≤ 2 (para que el nuevo sea ≤ 3)
- **Respuesta:** `Comment` con `depth` calculado

### CU-COM-02: Soft delete
- **Regla:** `isDeleted = true`, `content = NULL`. El comentario se retorna en queries con `isDeleted = true`, `content = null`. Hijos permanecen intactos.
- **Frontend:** Muestra "Este comentario ha sido eliminado"

### CU-COM-03: Consulta plana
- **Respuesta:** Lista plana `[Comment]` ordenada por `created_at ASC`
- **No incluir** comentarios con `post.deleted_at IS NOT NULL`
- **Cliente:** Construye árbol con algoritmo O(n)

---

## 6. Criterios de Aceptación

### CA-COM-01: Anidación válida
```
Given un comentario raíz en post (depth=0)
When se crea una respuesta (parentId=raíz)
Then depth=1, creación exitosa
When se crea respuesta a depth=1
Then depth=2, creación exitosa
When se crea respuesta a depth=2
Then depth=3, creación exitosa
When se crea respuesta a depth=3
Then error "Max depth reached"
```

### CA-COM-02: Soft delete preserva árbol
```
Given un comentario raíz con 2 respuestas
When el comentario raíz se elimina
Then isDeleted=true, content=null
And la query commentsByPost aún retorna el nodo raíz
And las 2 respuestas hijas aún son visibles
```

---

## 7. Matriz de Trazabilidad

| RF | Resolver | Service | Repository | Entity |
|----|---------|---------|------------|--------|
| RF-COM-01 | `CommentQueryResolver` | `CommentService` | `CommentRepository` | `Comment` |
| RF-COM-02 | `CommentMutationResolver` | `CommentService` | `CommentRepository` | `Comment` |
| RF-COM-05 | `CommentMutationResolver` | `LikeService` | `CommentLikeRepository` | `CommentLike` |
| RF-COM-06 | `CommentMutationResolver` | `CommentService` | `CommentRepository` | `Comment` |

---

## 8. Dependencias

| Dependencia | Tipo |
|-------------|------|
| `Comment` (entidad self-referencing) | Entidad |
| `CommentRepository` | Repositorio |
| `CommentLike` (entidad) | Entidad |
| `PostExistsValidator` | Validación |

---

## 9. Priorización

### MVP
RF-COM-01, RF-COM-02, RF-COM-04, RF-COM-06, RF-COM-07, RF-COM-08, RF-COM-11

### Fase 2
RF-COM-03, RF-COM-05, RF-COM-09, RF-COM-10

---

## 10. TODOs

| ID | Tarea | Prioridad | Esfuerzo |
|----|-------|-----------|----------|
| TODO-COM-01 | Implementar validación de profundidad con query recursiva | Alta | 4h |
| TODO-COM-02 | Migración Flyway: tabla comments y comment_likes | Alta | 3h |
| TODO-COM-03 | Índices: (post_id, parent_id, created_at), (user_id, comment_id) unique | Alta | 1h |
| TODO-COM-04 | Filtro global: WHERE is_deleted = false (para listado, pero incluir nodos padre eliminados con hijos) | Alta | 5h |
| TODO-COM-05 | Tests de integración: crear cadena de 3 niveles | Alta | 6h |
| TODO-COM-06 | Tests unitarios: validación profundidad, soft delete | Alta | 4h |
| TODO-COM-07 | Endpoint para reportar comentarios (admin moderation) | Media | 4h |
| TODO-COM-08 | Ordenamiento por "más votados" en commentsByPost | Media | 3h |
| TODO-COM-09 | DataLoader para comment likes (evitar N+1) | Media | 4h |
| TODO-COM-10 | Menciones @username: extraer y notificar | Baja | 6h |

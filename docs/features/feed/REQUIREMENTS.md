# Requerimientos: Muro de Publicaciones e Interacción (GraphQL)

## 1. Visión General

Módulo central que gestiona el CRUD de publicaciones estudiantiles, sistema de likes, algoritmo de trending (últimas 24h) y feed paginado con cursor. Implementado 100% sobre GraphQL.

---

## 2. Requerimientos Funcionales

| ID | Requerimiento | Prioridad | Estimación |
|----|--------------|-----------|------------|
| RF-FEED-01 | Exponer query `feedPosts(cursor, limit, filter)` con paginación cursor-based | Alta | 8 pts |
| RF-FEED-02 | Exponer query `postById(id: UUID!)` para detalle de publicación | Alta | 3 pts |
| RF-FEED-03 | Exponer mutation `createPost(input: PostInput!)` para crear publicaciones | Alta | 8 pts |
| RF-FEED-04 | Exponer mutation `updatePost(id: UUID!, input: PostInput!)` para editar | Alta | 5 pts |
| RF-FEED-05 | Exponer mutation `deletePost(id: UUID!)` para eliminar (soft delete) | Alta | 5 pts |
| RF-FEED-06 | Exponer mutation `likePost(postId: UUID!)` con toggle | Alta | 5 pts |
| RF-FEED-07 | Exponer query `trendingTopics` con top 10 de actividad (24h) | Alta | 10 pts |
| RF-FEED-08 | Validar contenido mínimo 10 y máximo 2000 caracteres | Alta | 3 pts |
| RF-FEED-09 | Validar soporte multimedia (max 5 archivos por post) | Alta | 8 pts |
| RF-FEED-10 | Implementar soft delete con `deletedAt` | Alta | 3 pts |
| RF-FEED-11 | Implementar unique constraint `(user_id, post_id)` para likes | Alta | 3 pts |
| RF-FEED-12 | Cachear trending con expiración configurable (default 15 min) | Media | 8 pts |
| RF-FEED-13 | Exponer query `searchPosts(query, filter)` para búsqueda (futuro) | Media | 10 pts |

**Total estimado:** 79 pts

---

## 3. Requerimientos No Funcionales

| ID | Requerimiento | Tipo | Métrica |
|----|--------------|------|---------|
| RNF-FEED-01 | Query `feedPosts` < 500ms (p95) con 10K publicaciones | Rendimiento | 500ms |
| RNF-FEED-02 | Mutation `createPost` < 1s (p95) | Rendimiento | 1s |
| RNF-FEED-03 | Trending cacheado, respuesta < 100ms | Rendimiento | 100ms |
| RNF-FEED-04 | Soporte para 100 queries simultáneas de feed | Escalabilidad | 100 concurrentes |
| RNF-FEED-05 | Like debe ser idempotente (misma respuesta si ya existe) | Consistencia | — |
| RNF-FEED-06 | Evitar N+1 en consultas de feed (usar @BatchMapping o DataLoader) | Rendimiento | — |
| RNF-FEED-07 | Soft delete no debe afectar consultas de feed (WHERE deleted_at IS NULL) | Consistencia | — |

---

## 4. Historias de Usuario

**HU-FEED-01:** Como estudiante, quiero publicar contenido para compartir con la comunidad.
**HU-FEED-02:** Como estudiante, quiero ver publicaciones recientes en orden cronológico.
**HU-FEED-03:** Como estudiante, quiero dar like a publicaciones para mostrar aprecio.
**HU-FEED-04:** Como estudiante, quiero ver temas populares para enterarme de lo que se discute.
**HU-FEED-05:** Como estudiante, quiero editar mi publicación si cometí un error.

---

## 5. Reglas de Negocio

| ID | Regla | Excepción |
|----|-------|-----------|
| RN-FEED-01 | Contenido entre 10 y 2000 caracteres | `ValidationException` |
| RN-FEED-02 | Like único por (usuario, publicación) | Unique constraint |
| RN-FEED-03 | Edición solo hasta 24h post-creación | `EditWindowExpiredException` |
| RN-FEED-04 | Soft delete: `deleted_at` not null | — |
| RN-FEED-05 | Trending score = (likes_24h * 0.4) + (comments_24h * 0.6) | — |
| RN-FEED-06 | Máximo 5 archivos multimedia por post | `ValidationException` |
| RN-FEED-07 | Máximo 10 tags por post | `ValidationException` |

---

## 6. Casos de Uso

### CU-FEED-01: Crear publicación
- **Mutation:** `createPost(input: PostInput!)`
- **Input:** `PostInput { content, media[], tags[] }`
- **Validaciones:** content 10-2000, media ≤ 5, tags ≤ 10
- **Respuesta:** `Post` completo con id, timestamps, author
- **Postcondición:** Publicación persistida, trending recalculado async

### CU-FEED-02: Feed paginado
- **Query:** `feedPosts(cursor, limit: 20, filter: { tag, authorId })`
- **Implementación:** Cursor = `createdAt` + `id` del último post
- **Respuesta:** `PostConnection { edges: [Post], cursor, hasNext }`

### CU-FEED-03: Like toggle
- **Mutation:** `likePost(postId)`
- **Regla:** Busca `PostLike` existente. Si existe → delete, si no → insert
- **Respuesta:** `LikePayload { liked: Boolean!, likesCount: Int! }`

### CU-FEED-04: Trending topics
- **Query:** `trendingTopics`
- **Cálculo:** `SELECT tag, COUNT(*) as posts, SUM(score) as total FROM posts WHERE created_at > NOW() - INTERVAL '24 hours'`
- **Cache:** En memoria o Redis, expira cada 15 min
- **Respuesta:** `[TrendingTopic]` top 10

---

## 7. Criterios de Aceptación

### CA-FEED-01: Feed paginado
```
Given 50 publicaciones en BD
When se consulta feedPosts(limit: 20)
Then retorna 20 posts con cursor del último
When se consulta con ese cursor
Then retorna los siguientes 20 posts con hasNext=true
When se consulta con el último cursor
Then retorna los últimos 10 posts con hasNext=false
```

### CA-FEED-02: Like toggle idempotente
```
Given una publicación sin like del usuario
When se ejecuta likePost(postId)
Then retorna { liked: true, likesCount: N+1 }
When se ejecuta likePost(postId) nuevamente
Then retorna { liked: false, likesCount: N }
```

### CA-FEED-03: Trending cacheado
```
Given 5 publicaciones con actividad reciente
When se consulta trendingTopics
Then retorna top 5 ordenados por score descendente
When se consulta nuevamente en < 15 min
Then retorna mismo resultado (cache hit)
```

---

## 8. Matriz de Trazabilidad

| RF | GraphQL Resolver | Service | Repository | Entity |
|----|-----------------|---------|------------|--------|
| RF-FEED-01 | `PostQueryResolver` | `FeedService` | `PostRepository` | `Post` |
| RF-FEED-03 | `PostMutationResolver` | `PostService` | `PostRepository` | `Post` |
| RF-FEED-06 | `LikeMutationResolver` | `LikeService` | `PostLikeRepository` | `PostLike` |
| RF-FEED-07 | `TrendingQueryResolver` | `TrendingService` | `PostRepository` | `Post` |
| RF-FEED-10 | `PostMutationResolver` | `PostService` | `PostRepository` | `Post` (soft delete) |

---

## 9. Dependencias

| Dependencia | Tipo |
|-------------|------|
| `spring-boot-starter-graphql` | Librería |
| `Post`, `PostMedia`, `PostLike`, `PostTag` | Entidades |
| `PostRepository`, `PostLikeRepository` | Repositorios |
| `FileStorageService` | Servicio |
| `TrendingService` | Servicio interno |
| Cache (Redis o Caffeine) | Infraestructura |
| `DataLoader` / `@BatchMapping` | Optimización |

---

## 10. Priorización

### MVP (Sprint 1-2)
RF-FEED-01, RF-FEED-02, RF-FEED-03, RF-FEED-05, RF-FEED-06, RF-FEED-08, RF-FEED-10, RF-FEED-11

### Fase 2 (Sprint 3-4)
RF-FEED-04, RF-FEED-07, RF-FEED-09, RF-FEED-12

### Fase 3 (Post-MVP)
RF-FEED-13 (búsqueda full-text)

---

## 11. TODOs

| ID | Tarea | Prioridad | Esfuerzo | Notas |
|----|-------|-----------|----------|-------|
| TODO-FEED-01 | Implementar DataLoader para evitar N+1 en feed | Alta | 8h | @BatchMapping en author, likesCount |
| TODO-FEED-02 | Cache de trending con Caffeine (Spring Cache) | Alta | 4h | @Cacheable("trending") |
| TODO-FEED-03 | Índices compuestos en BD para feed queries | Alta | 2h | (created_at DESC), (user_id, created_at) |
| TODO-FEED-04 | Soft delete filter global (WHERE deleted_at IS NULL) | Alta | 3h | @Where(clause = "deleted_at IS NULL") |
| TODO-FEED-05 | Validación de ventana de edición (24h) | Alta | 3h | Comparar createdAt vs now() |
| TODO-FEED-06 | Migración Flyway para tablas de feed | Alta | 4h | V2__create_posts_tables.sql |
| TODO-FEED-07 | Tests de integración: mutations feed | Alta | 10h | GraphQLTestTemplate |
| TODO-FEED-08 | Búsqueda full-text con PostgreSQL tsvector | Media | 10h | Índice GIN + función de búsqueda |
| TODO-FEED-09 | Posts fijados (pinned) con prioridad en feed | Media | 5h | Campo isPinned + ORDER BY |
| TODO-FEED-10 | Encuestas dentro de publicaciones | Baja | 10h | Nueva entidad PostPoll + PollOption |
| TODO-FEED-11 | Migrar trending cache a Redis en producción | Media | 4h | RedisTemplate + @CacheConfig |
| TODO-FEED-12 | Tests unitarios: TrendingService, LikeService | Alta | 6h | Mockito + JUnit 5 |

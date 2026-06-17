# Módulo: Muro de Publicaciones e Interacción (GraphQL)

## 1. Objetivo del módulo

Gestionar la creación de publicaciones y debates estudiantiles en el muro principal, incorporando un panel de descubrimiento con los temas más activos de las últimas 24 horas.

## 2. Alcance

- **Incluye:** CRUD de publicaciones, likes, sistema de trending, panel de descubrimiento.
- **Incluye:** Feed paginado con cursor, soporte multimedia en posts.
- **No incluye:** Comentarios (módulo separado), notificaciones (módulo separado).

## 3. Lógica de negocio

- **Publicaciones:**
  - Todo estudiante autenticado puede crear, editar y eliminar sus propias publicaciones.
  - Las publicaciones pueden incluir texto (Markdown), imágenes y archivos.
  - Soft delete: al eliminar, el contenido se oculta pero persiste en BD.
- **Likes:**
  - Acción toggle: un solo like por usuario por publicación.
  - Se registra timestamp para métricas de trending.
- **Trending (24h):**
  - Algoritmo de ponderación: `score = (likes_24h * 0.4) + (comments_24h * 0.6)`.
  - Se actualiza cada 15 minutos (caché en Redis o en memoria).
  - Panel lateral muestra top 10 temas con más actividad.

## 4. Validaciones

### Entrada

- `PostInput.content`: requerido, min 10, max 2000.
- `PostInput.media`: opcional, máximo 5 archivos, formatos: JPG, PNG, WEBP, PDF.
- `PostInput.tags`: opcional, máximo 10 tags, min 2 caracteres cada uno.

### Reglas de dominio

- Un usuario solo puede dar like una vez por post (unique constraint `user_id + post_id`).
- El autor puede editar hasta 24h después de publicado (ventana de edición configurable).
- Soft delete: `deleted_at` no nulo, el post no se retorna en queries de feed.

## 5. Contratos API

### GraphQL

```graphql
type Query {
    feedPosts(cursor: String, limit: Int, filter: PostFilter): PostConnection!
    postById(id: UUID!): Post
    trendingTopics: [TrendingTopic!]!
}

type Mutation {
    createPost(input: PostInput!): Post!
    updatePost(id: UUID!, input: PostInput!): Post!
    deletePost(id: UUID!): Boolean!
    likePost(postId: UUID!): LikePayload!
}

type Post {
    id: UUID!
    content: String!
    author: User!
    media: [Media!]
    tags: [String!]
    likesCount: Int!
    commentsCount: Int!
    isLikedByMe: Boolean!
    createdAt: DateTime!
    updatedAt: DateTime
}

type TrendingTopic {
    tag: String!
    postCount: Int!
    score: Float!
    lastActivity: DateTime!
}
```

### REST

- No aplica (todo el feed es GraphQL).

## 6. Persistencia

- Entidades: `Post`, `PostMedia`, `PostLike`, `PostTag`, `TrendingCache`.
- Repositorios: `PostRepository`, `PostLikeRepository`, `PostTagRepository`.
- `Post` → `PostMedia` (1:M), `Post` → `PostLike` (1:M).
- Índices: `(user_id, created_at)` para feed, `(post_id, user_id)` único para likes.

## 7. Seguridad

- `createPost`, `updatePost`, `deletePost`, `likePost` — autenticado.
- `feedPosts`, `postById`, `trendingTopics` — público (lectura).
- `updatePost`/`deletePost` validan ownership del autor.

## 8. Errores y excepciones

| Escenario | Excepción | Código GraphQL |
|-----------|-----------|----------------|
| Post no encontrado | `ResourceNotFoundException` | NOT_FOUND |
| No autorizado a editar | `AccessDeniedException` | FORBIDDEN |
| Like duplicado (idempotente) | — | OK (retorna estado actual) |
| Contenido vacío | `ValidationException` | BAD_REQUEST |

## 9. Dependencias del módulo

- `PostRepository`, `PostLikeRepository`, `PostTagRepository`
- `TrendingService` — algoritmo de ponderación y caché
- `FileStorageService` — almacenamiento multimedia
- `UserRepository` — validación de autor

## 10. Observabilidad

- Métrica: posts creados por hora, densidad de likes.
- Loggear eliminaciones de posts (userId, postId, motivo si aplica).
- Cache hit/miss ratio del trending.

## 11. Casos de prueba sugeridos

- Creación de post exitosa retorna Post con datos correctos.
- Like toggle: like → unlike → like refleja conteo correcto.
- Feed paginado retorna `PostConnection` con cursor y hasNext.
- Post de otro usuario no puede ser editado.
- Trending incluye solo actividad de últimas 24h.

## 12. TODO / Pendientes

- Implementar caché de trending en Redis (actualmente en memoria).
- Algoritmo de trending con decay temporal (no solo ventana fija).
- Posts fijados (pinned) para comunicados institucionales.
- Soporte para encuestas en publicaciones.

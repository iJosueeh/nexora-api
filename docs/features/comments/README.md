# Módulo: Sistema de Comentarios Anidados

## 1. Objetivo del módulo

Organizar las respuestas de las publicaciones en una estructura de árbol jerárquico, implementando un algoritmo optimizado en memoria para construir la jerarquía en el cliente sin sobrecargar el servidor.

## 2. Alcance

- **Incluye:** CRUD de comentarios, anidación jerárquica (hasta 3 niveles), likes en comentarios.
- **Incluye:** Algoritmo de armado de árbol en memoria (servidor retorna lista plana, cliente construye jerarquía).
- **No incluye:** Moderación de comentarios, spam detection, menciones (@usuario).

## 3. Lógica de negocio

- **Creación:**
  - Comentario raíz: `parentId = null`.
  - Respuesta: `parentId = UUID` del comentario padre.
  - Se valida profundidad máxima (3 niveles incluido el raíz).
- **Árbol en cliente:**
  - El servidor retorna comentarios como lista plana con `parentId`.
  - El cliente ejecuta algoritmo en memoria: `Map<parentId, Comment[]>` en O(n).
  - Esto evita consultas recursivas N+1 en el servidor.
- **Eliminación:**
  - Soft delete: el contenido se reemplaza por "[eliminado]" pero el nodo persiste para mantener el árbol.
  - Los hijos de un comentario eliminado aún son visibles.
- **Likes:** toggle, único por usuario por comentario.

## 4. Validaciones

### Entrada

- `CommentInput.content`: requerido, min 1, max 1000.
- `CommentInput.postId`: UUID, debe existir.
- `CommentInput.parentId`: opcional, UUID, debe existir y no exceder profundidad.

### Reglas de dominio

- Profundidad máxima: 3 niveles (raíz → nivel 1 → nivel 2 → nivel 3).
- Un usuario solo puede dar like una vez por comentario.
- Edición permitida hasta 1 hora después de la creación.
- Soft delete: `isDeleted = true`, contenido se oculta.

## 5. Contratos API

### GraphQL

```graphql
type Query {
    commentsByPost(postId: UUID!, cursor: String, limit: Int): CommentConnection!
}

type Mutation {
    createComment(input: CommentInput!): Comment!
    updateComment(id: UUID!, content: String!): Comment!
    deleteComment(id: UUID!): Boolean!
    likeComment(commentId: UUID!): LikePayload!
}

type Comment {
    id: UUID!
    content: String!
    author: User!
    postId: UUID!
    parentId: UUID
    depth: Int!
    likesCount: Int!
    isLikedByMe: Boolean!
    isDeleted: Boolean!
    createdAt: DateTime!
    updatedAt: DateTime
}

type CommentConnection {
    edges: [Comment!]!
    cursor: String
    hasNext: Boolean!
}
```

### REST

- No aplica (todo es GraphQL).

## 6. Persistencia

- Entidad: `Comment`.
- Repositorio: `CommentRepository`.
- Índices: `(post_id, parent_id, created_at)` para recuperación eficiente por post.
- `Comment` se relaciona M:1 con `Post` y M:1 con `User` (author).
- `parentId` es self-referencing (`Comment → Comment`).

## 7. Seguridad

- `commentsByPost` — público.
- `createComment`, `updateComment`, `deleteComment`, `likeComment` — autenticado.
- `updateComment`/`deleteComment` validan ownership del autor.

## 8. Errores y excepciones

| Escenario | Excepción | Código GraphQL |
|-----------|-----------|----------------|
| Post no encontrado | `ResourceNotFoundException` | NOT_FOUND |
| Comentario no encontrado | `ResourceNotFoundException` | NOT_FOUND |
| Profundidad máxima excedida | `MaxDepthExceededException` | BAD_REQUEST |
| No autorizado a editar | `AccessDeniedException` | FORBIDDEN |
| Ventana de edición expirada | `EditWindowExpiredException` | BAD_REQUEST |

## 9. Dependencias del módulo

- `CommentRepository`
- `PostRepository` — validación de post existente
- Algoritmo de árbol en cliente (no requiere endpoint adicional)

## 10. Observabilidad

- Métrica: profundidad promedio de hilos de comentarios.
- Loggear eliminaciones de comentarios (userId, commentId).
- Tiempo de resolución de `commentsByPost`.

## 11. Casos de prueba sugeridos

- Crear comentario raíz en post existente.
- Crear respuesta a comentario (nivel 2).
- Intentar crear respuesta a nivel 3 → error.
- Soft delete: contenido oculto pero hijos visibles.
- Like toggle refleja cambio de conteo.
- Consulta `commentsByPost` retorna lista plana ordenada.

## 12. TODO / Pendientes

- Implementar lazy loading de hilos profundos (más de 10 respuestas).
- Moderación: endpoint para reportar comentarios (admin).
- Menciones con @username: notificación al usuario mencionado.
- Algoritmo de ordenamiento: más recientes vs. más votados.

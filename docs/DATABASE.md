# Nexora — Base de Datos

## Tablas principales

| Tabla | Propósito | Descripción | Relaciones clave |
|---|---|---|---|
| `usuarios` | Cuentas de usuario | Almacena los usuarios autenticados vía Supabase Auth. Contiene email, rol, estado activo/inactivo. | FK a `roles`; referenciado por ~15 tablas (posts, comentarios, eventos, etc.) |
| `perfiles` | Perfiles públicos | Información pública del usuario: username, nombre, bio, avatar, banner, carrera. Vinculación 1:1 con `usuarios`. | FK a `carreras`; colección `perfiles_intereses` a `intereses_academicos` |
| `roles` | Roles del sistema | Catálogo de roles (ADMIN, USER, OFFICIAL). | Referenciado por `usuarios` |
| `facultades` | Facultades universitarias | Catálogo de facultades. | Referenciado por `carreras` |
| `carreras` | Carreras/programas | Catálogo de carreras asociadas a una facultad. | FK a `facultades`; referenciado por `perfiles` |
| `intereses_academicos` | Intereses académicos | Catálogo de etiquetas de interés académico. | Colección desde `perfiles` vía `perfiles_intereses` |
| `posts` | Publicaciones del feed | Contenido principal: texto, título opcional, imagen, ubicación, estado official, etiquetas. | FK a `usuarios`; referenciado por `comentarios`, `post_likes`, `bookmarks`, `notifications` |
| `post_tags` | Etiquetas de posts | Tags asociados a cada post (ElementCollection). | FK a `posts` |
| `comentarios` | Comentarios en posts | Sistema de comentarios con replies anidados (autorreferenciado). Notifica al dueño del post vía trigger. | FK a `posts` y `usuarios`; self-FK `parent_id` para replies |
| `post_likes` | Likes en posts | Usuarios que dieron like a un post. Trigger crea/elimina notificaciones. | FK a `posts` y `usuarios` |
| `notifications` | Notificaciones | Sistema de notificaciones (LIKE, COMMENT, COMMENT_REPLY, FOLLOW). Timezone Lima. | FK a `usuarios` (recipient y sender), FK opcional a `posts` y `university_events` |
| `seguidores` | Seguidores | Relación seguir/dejar de seguir entre usuarios. Trigger notifica al seguir. | FK a `usuarios` (follower/following) |
| `research_papers` | Recursos académicos | Publicaciones tipo investigación/paper con slug, resumen, facultad, contador de vistas, PDF opcional. Búsqueda full-text con tsvector. | FK a `usuarios` |
| `university_events` | Eventos universitarios | Eventos con fecha, ubicación, categoría, organizador, links de comunidad. Búsqueda full-text con tsvector. | FK a `usuarios`; referenciado por `event_attendees` y `notifications` |
| `event_attendees` | Asistentes a eventos | Relación N:N entre usuarios y eventos (RSVP). | FK a `university_events` y `usuarios` |
| `bookmarks` | Posts guardados | Usuarios guardan posts para ver después. Unique(user_id, post_id). | FK a `usuarios` y `posts` |
| `study_groups` | Grupos de estudio | Grupos con nombre, descripción, categoría, privacidad, límite de miembros. | FK a `usuarios` (creador) |
| `group_memberships` | Membresías de grupos | Usuarios en grupos con rol (MEMBER, MODERATOR, OWNER) y estado (APPROVED, PENDING). | FK a `study_groups` y `usuarios` |

## Diagrama de relaciones

```
usuarios ──> roles

perfiles ──> usuarios (1:1)
perfiles ──> carreras
perfiles ──> intereses_academicos (M:N vía perfiles_intereses)

carreras ──> facultades

posts ──> usuarios
posts ──> post_tags (collection)

comentarios ──> posts
comentarios ──> usuarios
comentarios ──> comentarios (parent_id, self-ref)

post_likes ──> posts + usuarios

notifications ──> usuarios (recipient + sender)
notifications ──> posts (opcional)
notifications ──> university_events (opcional)

seguidores ──> usuarios (follower + following)

research_papers ──> usuarios

university_events ──> usuarios
event_attendees ──> university_events + usuarios

bookmarks ──> usuarios + posts

study_groups ──> usuarios
group_memberships ──> study_groups + usuarios
```

## Migraciones Flyway

| Migración | Cambio |
|---|---|
| V1 | Tablas iniciales: `research_papers`, `university_events`, `event_attendees` |
| V2 | `post_likes`, `notifications` + trigger de like |
| V3 | Trigger cleanup de unlike |
| V4 | `seguidores` + trigger de follow |
| V5 | Fix trigger de follow |
| V6 | Fix timezone Lima + deduplicación |
| V7 | Auditoría: `updated_at` en tablas existentes |
| V8 | `supabase_id` en `usuarios` |
| V9 | `author_id` y `capacity` en `university_events` |
| V10 | `bookmarks` |
| V11 | Full-text search en `posts` (tsvector + GIN + trigger) |
| V12 | `study_groups` + `group_memberships` |
| V13 | Full-text search en `university_events` y `research_papers` |

> **Nota:** Las tablas `posts`, `comentarios`, `perfiles`, `roles`, `facultades`, `carreras`, `intereses_academicos` y `usuarios` se crean fuera de Flyway (esquema base/Supabase). Flyway solo administra tablas complementarias.

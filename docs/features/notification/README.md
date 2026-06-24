# Módulo: Notificaciones en Vivo (WebSocket)

## 1. Objetivo del módulo

Enviar alertas instantáneas y asíncronas sobre interacciones sociales (nuevos "me gusta", seguidores, comentarios) mediante conexiones WebSocket persistentes, eliminando la necesidad de recargar la pantalla.

## 2. Alcance

- **Incluye:** Envío de notificaciones en tiempo real vía WebSocket, tipos de notificación, marcado como leído, reconexión automática.
- **Incluye:** Agrupación de notificaciones similares en una sola entrada.
- **No incluye:** Notificaciones push nativas (Web Push API), email notifications.

## 3. Lógica de negocio

- **Disparo de notificaciones:**
  - Se genera una notificación cuando ocurre una acción social sobre el contenido de un usuario.
  - Tipos: `LIKE`, `COMMENT`, `REPLY`, `FOLLOW`, `MENTION`.
  - El creador del contenido no recibe notificación de su propia acción.
- **WebSocket:**
  - Conexión persistente autenticada con JWT (query param `?token=...`).
  - Cada usuario recibe eventos en un canal personal: `/topic/notifications/{userId}`.
  - El servidor envía el payload completo de la notificación.
- **Agrupación:**
  - Múltiples likes en el mismo post dentro de 5 minutos se agrupan: "A X personas les gustó tu publicación".
- **Estado leído:**
  - Notificaciones no leídas persisten en BD.
  - Se marcan como leídas al abrir el centro de notificaciones o individualmente.

## 4. Validaciones

### Entrada

- `NotificationSettings.type`: enum con tipos de notificación habilitados.

### Reglas de dominio

- No auto-notificarse (el autor no recibe notificación de su propio like).
- Límite de agrupación: 5 minutos desde la primera notificación del grupo.
- Las notificaciones se eliminan físicamente después de 90 días (cleanup job).

## 5. Contratos API

### WebSocket (STOMP)

```
SUBSCRIBE /topic/notifications/{userId}
  Header: Authorization: Bearer {jwt}

Payload:
{
  "id": "uuid",
  "type": "LIKE" | "COMMENT" | "REPLY" | "FOLLOW" | "MENTION",
  "actor": { "id": "uuid", "username": "string", "avatarUrl": "string" },
  "targetId": "uuid",
  "targetType": "POST" | "COMMENT" | "USER",
  "message": "A @username le gustó tu publicación",
  "groupCount": 3,
  "isRead": false,
  "createdAt": "datetime"
}
```

### GraphQL

```graphql
type Query {
    myNotifications(cursor: String, limit: Int, unreadOnly: Boolean): NotificationConnection!
    unreadNotificationCount: Int!
}

type Mutation {
    markNotificationAsRead(id: UUID!): Boolean!
    markAllNotificationsAsRead: Boolean!
    updateNotificationSettings(settings: NotificationSettingsInput!): NotificationSettings!
}
```

## 6. Persistencia

- Entidad: `Notification`, `NotificationGroup`, `NotificationSettings`.
- Repositorios: `NotificationRepository`, `NotificationSettingsRepository`.
- `Notification` → `User` (recipient M:1), `User` (actor M:1).
- Índices: `(recipient_id, created_at DESC)` para consulta eficiente.
- Cleanup programado: `@Scheduled` diario, elimina notificaciones > 90 días.

## 7. Seguridad

- WebSocket autenticado con JWT.
- Solo el destinatario puede leer/modificar sus notificaciones.
- Validación de ownership: el userId del canal WS debe coincidir con el JWT.

## 8. Errores y excepciones

| Escenario | Excepción | Código |
|-----------|-----------|--------|
| Token WS inválido | `AuthenticationException` | 401 |
| Canal no autorizado | `AccessDeniedException` | 403 |
| Notificación no encontrada | `ResourceNotFoundException` | NOT_FOUND |

## 9. Dependencias del módulo

- `WebSocketConfig` — configuración STOMP
- `JwtService` — autenticación de conexiones WS
- `NotificationRepository`, `NotificationSettingsRepository`
- `UserRepository` — validación de actores

## 10. Observabilidad

- Métrica: notificaciones enviadas por minuto (por tipo).
- Latencia de entrega WS (end-to-end).
- Loggear reconexiones fallidas.
- Métrica: notificaciones agrupadas vs. individuales.

## 11. Casos de prueba sugeridos

- Like en post genera notificación al autor (no a sí mismo).
- Comentario en post genera notificación al autor del post.
- Múltiples likes agrupados en una notificación.
- Marcar como leído actualiza el estado.
- Usuario no autenticado no puede conectar WS.
- Cleanup diario elimina notificaciones antiguas.

## 12. TODO / Pendientes

- Implementar Web Push API para notificaciones nativas.
- Preferencias granulares por tipo de notificación.
- Soporte para notificaciones de eventos próximos y recordatorios.
- Cola de eventos pendientes con replay al reconectar.

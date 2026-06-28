# Requerimientos: Notificaciones en Vivo (WebSocket)

## 1. Visión General

Sistema de notificaciones asíncronas en tiempo real vía WebSocket (STOMP) que alerta a los estudiantes sobre interacciones sociales. Incluye agrupación de eventos, persistencia en BD y limpieza programada.

---

## 2. Requerimientos Funcionales

| ID | Requerimiento | Prioridad | Estimación |
|----|--------------|-----------|------------|
| RF-NOT-01 | Establecer conexión WebSocket STOMP autenticada con JWT | Alta | 8 pts |
| RF-NOT-02 | Enviar notificación en tiempo real al recibir un like | Alta | 5 pts |
| RF-NOT-03 | Enviar notificación al recibir un comentario en publicación propia | Alta | 5 pts |
| RF-NOT-04 | Enviar notificación al recibir un follow | Alta | 5 pts |
| RF-NOT-05 | Enviar notificación al recibir respuesta a comentario propio | Alta | 5 pts |
| RF-NOT-06 | Exponer query `myNotifications(cursor, limit, unreadOnly)` con paginación | Alta | 8 pts |
| RF-NOT-07 | Exponer mutation `markNotificationAsRead(id)` y `markAllAsRead` | Alta | 5 pts |
| RF-NOT-08 | Exponer query `unreadNotificationCount` para badge | Alta | 3 pts |
| RF-NOT-09 | Agrupar notificaciones del mismo tipo/target en ventana de 5 min | Media | 10 pts |
| RF-NOT-10 | No generar notificación para auto-interacciones | Alta | 3 pts |
| RF-NOT-11 | Exponer mutation `updateNotificationSettings` para preferencias | Media | 5 pts |
| RF-NOT-12 | Programar limpieza diaria de notificaciones > 90 días | Media | 3 pts |

**Total estimado:** 65 pts

---

## 3. Requerimientos No Funcionales

| ID | Requerimiento | Tipo | Métrica |
|----|--------------|------|---------|
| RNF-NOT-01 | Latencia WS < 500ms desde evento hasta entrega (p95) | Rendimiento | 500ms |
| RNF-NOT-02 | Conexión WS autenticada con JWT en query param `?token=` | Seguridad | — |
| RNF-NOT-03 | Heartbeat STOMP cada 30s | Confiabilidad | 30s |
| RNF-NOT-04 | Cleanup diario con `@Scheduled` (cron 0 0 3 * * ?) | Mantenimiento | — |
| RNF-NOT-05 | Canal personal: `/topic/notifications/{userId}` | Seguridad | — |
| RNF-NOT-06 | Validar que el userId del canal coincida con el JWT | Seguridad | — |
| RNF-NOT-07 | Soportar 1000 conexiones WS concurrentes | Escalabilidad | 1000 |

---

## 4. Reglas de Negocio

| ID | Regla | Excepción |
|----|-------|-----------|
| RN-NOT-01 | Auto-exclusión: `actorId != recipientId` | — |
| RN-NOT-02 | Agrupación: mismo `(type, targetId, recipientId)` en 5 min | — |
| RN-NOT-03 | Tipos válidos: `LIKE, COMMENT, REPLY, FOLLOW, MENTION` | `InvalidNotificationTypeException` |
| RN-NOT-04 | Retención: 90 días, luego eliminación física | — |
| RN-NOT-05 | Canal seguro: validar JWT → userId | `AuthenticationException` |

---

## 5. Casos de Uso

### CU-NOT-01: Like → notificación en vivo
1. `LikeService.likePost()` ejecuta éxito
2. Publica `NotificationEvent` en `ApplicationEventPublisher`
3. `NotificationEventListener` escucha, persiste `Notification`, envía por WS
4. Payload: `{ type: "LIKE", actor: {...}, targetId, message, groupCount, isRead, createdAt }`

### CU-NOT-02: Agrupación de notificaciones
1. Usuario B da like a post de A → notificación individual
2. Usuario C da like al mismo post en < 5 min → se agrupa con la anterior
3. `groupCount` se incrementa a 2, mensaje → "A 2 personas les gustó tu publicación"
4. Si pasa > 5 min, nueva notificación individual

### CU-NOT-03: Cleanup diario
```
@Scheduled(cron = "0 0 3 * * ?")
deleteNotificationsOlderThan(90 days)
```

---

## 6. Criterios de Aceptación

### CA-NOT-01: Entrega en vivo
```
Given Usuario A y B con WS conectados
When A da like al post de B
Then B recibe payload en /topic/notifications/{B.id}
And la notificación se persiste en BD
```

### CA-NOT-02: Agrupación temporal
```
Given 3 usuarios dan like al mismo post de A en 3 minutos
When se consulta myNotifications(unreadOnly: true)
Then aparece 1 notificación con groupCount=3
And mensaje "A 3 personas les gustó tu publicación"
```

---

## 7. Matriz de Trazabilidad

| RF | WebSocket/Resolver | Service | Repository | Entity |
|----|-------------------|---------|------------|--------|
| RF-NOT-01 | `WebSocketConfig` | `JwtService` | — | — |
| RF-NOT-02 | `NotificationEventPublisher` | `NotificationService` | `NotificationRepository` | `Notification` |
| RF-NOT-06 | `NotificationQueryResolver` | `NotificationService` | `NotificationRepository` | `Notification` |
| RF-NOT-07 | `NotificationMutationResolver` | `NotificationService` | `NotificationRepository` | `Notification` |
| RF-NOT-09 | `NotificationGroupingService` | `NotificationService` | `NotificationRepository` | `NotificationGroup` |
| RF-NOT-12 | `NotificationCleanupJob` | `NotificationService` | `NotificationRepository` | `Notification` |

---

## 8. Dependencias

| Dependencia | Tipo |
|-------------|------|
| `spring-boot-starter-websocket` | Librería |
| `spring-context` (event publisher) | Librería |
| `Notification`, `NotificationGroup`, `NotificationSettings` | Entidades |
| `NotificationRepository` | Repositorio |
| `JwtService` | Servicio |
| `UserRepository` | Repositorio |

---

## 9. Priorización

### MVP
RF-NOT-01, RF-NOT-02, RF-NOT-03, RF-NOT-06, RF-NOT-07, RF-NOT-08, RF-NOT-10, RF-NOT-11

### Fase 2
RF-NOT-04, RF-NOT-05, RF-NOT-09, RF-NOT-12

---

## 10. TODOs

| ID | Tarea | Prioridad | Esfuerzo | Notas |
|----|-------|-----------|----------|-------|
| TODO-NOT-01 | Implementar WebSocket STOMP config con JWT auth | Alta | 8h | Interceptor de canal |
| TODO-NOT-02 | Implementar `NotificationEventPublisher` + listener async | Alta | 6h | @Async + @EventListener |
| TODO-NOT-03 | Lógica de agrupación con ventana de 5 min | Media | 8h | Comparar timestamps, upsert groupCount |
| TODO-NOT-04 | Scheduled job de limpieza (90 días) | Media | 3h | @Scheduled + DELETE FROM |
| TODO-NOT-05 | Migración Flyway: tablas notifications + notification_settings | Alta | 4h | V3__create_notifications.sql |
| TODO-NOT-06 | Índices en (recipient_id, created_at DESC), (type, target_id) | Alta | 1h | Performance |
| TODO-NOT-07 | Tests de integración: evento → persistencia → WS | Alta | 10h | Mock WebSocket session |
| TODO-NOT-08 | Tests unitarios: agrupación, auto-exclusión, cleanup | Alta | 6h | Mockito |
| TODO-NOT-09 | Configuración de heartbeat y timeout en WebSocket config | Alta | 2h | taskExecutor |
| TODO-NOT-10 | Monitoreo de conexiones WS activas (métrica) | Media | 4h | Actuator + métrica custom |
| TODO-NOT-11 | Preferencias granulares por tipo de notificación | Media | 5h | CRUD NotificationSettings |

# Requerimientos: Eventos y Grupos de Estudio Temáticos

## 1. Visión General

API GraphQL para la gestión de eventos académicos y grupos de estudio. Permite CRUD de eventos, RSVP con control de capacidad, y CRUD de grupos con membresías por roles (OWNER, MODERATOR, MEMBER).

---

## 2. Requerimientos Funcionales

| ID | Requerimiento | Prioridad | Estimación |
|----|--------------|-----------|------------|
| RF-EVT-01 | Exponer query `events(cursor, limit, filter)` con filtros | Alta | 8 pts |
| RF-EVT-02 | Exponer query `eventBySlug(slug)` para detalle público | Alta | 3 pts |
| RF-EVT-03 | Exponer mutation `createEvent(input)` para crear eventos | Alta | 8 pts |
| RF-EVT-04 | Exponer mutation `updateEvent(id, input)` para editar (organizer) | Alta | 5 pts |
| RF-EVT-05 | Exponer mutation `deleteEvent(id)` (organizer) | Alta | 3 pts |
| RF-EVT-06 | Exponer mutation `rsvpEvent(eventId)` con toggle | Alta | 5 pts |
| RF-EVT-07 | Exponer query `groups(cursor, limit, filter)` para explorar | Alta | 8 pts |
| RF-EVT-08 | Exponer query `groupBySlug(slug)` para detalle | Alta | 3 pts |
| RF-EVT-09 | Exponer mutation `createGroup(input)` para crear grupos | Alta | 8 pts |
| RF-EVT-10 | Exponer mutation `joinGroup(groupId)` y `leaveGroup(groupId)` | Alta | 5 pts |
| RF-EVT-11 | Exponer mutation `approveMembership(membershipId)` (OWNER/MODERATOR) | Media | 5 pts |
| RF-EVT-12 | Validar capacidad máxima del evento antes de RSVP | Alta | 3 pts |
| RF-EVT-13 | Validar límites: 10 grupos/estudiante, 50 miembros/grupo | Alta | 3 pts |

**Total estimado:** 67 pts

---

## 3. Requerimientos No Funcionales

| ID | Requerimiento | Tipo | Métrica |
|----|--------------|------|---------|
| RNF-EVT-01 | Queries de listado < 500ms con filtros | Rendimiento | 500ms |
| RNF-EVT-02 | RSVP < 300ms (p95) | Rendimiento | 300ms |
| RNF-EVT-03 | RSVP único por (usuario, evento) | Consistencia | Unique |
| RNF-EVT-04 | Slug único para eventos y grupos | Consistencia | Unique |
| RNF-EVT-05 | Evento pasado no acepta RSVP | Regla | — |
| RNF-EVT-06 | Solo OWNER edita/elimina grupo | Seguridad | — |

---

## 4. Reglas de Negocio

| ID | Regla | Excepción |
|----|-------|-----------|
| RN-EVT-01 | RSVP único por (usuario, evento) | Unique constraint |
| RN-EVT-02 | Capacidad: `attendeesCount < capacity` | `EventFullException` |
| RN-EVT-03 | Límite: 10 grupos por estudiante | `MaxGroupsLimitException` |
| RN-EVT-04 | Límite: 50 miembros por grupo | `GroupFullException` |
| RN-EVT-05 | Evento futuro: `startDate > now()` | `ValidationException` |
| RN-EVT-06 | Solo OWNER/MODERATOR pueden aprobar membresías | `AccessDeniedException` |

---

## 5. Casos de Uso

### CU-EVT-01: RSVP con verificación de capacidad
1. Mutation `rsvpEvent(eventId)`
2. Sistema verifica si ya existe `EventAttendee` para el usuario
3. Si existe → elimina (cancela), decrementa `attendeesCount`
4. Si no existe → verifica `attendeesCount < capacity`
5. Si hay cupo → crea `EventAttendee`, incrementa `attendeesCount`
6. Retorna `RSVPPayload { isAttending, attendeesCount }`

### CU-EVT-02: Membresía por solicitud (grupo privado)
1. Mutation `joinGroup(groupId)`
2. Sistema verifica `Group.joinMode`
3. Si `FREE` → crea membresía directa como `MEMBER`
4. Si `REQUEST` → crea membresía con `status = PENDING`
5. OWNER recibe notificación para aprobar
6. OWNER ejecuta `approveMembership(membershipId)` → `status = APPROVED`

---

## 6. Criterios de Aceptación

### CA-EVT-01: RSVP cuando evento lleno
```
Given evento con capacity=2 y 2 asistentes
When un tercer estudiante intenta RSVP
Then sistema retorna EventFullException
And attendeeCount sigue siendo 2
```

### CA-EVT-02: Límite de grupos alcanzado
```
Given un estudiante ya en 10 grupos
When intenta unirse a un 11º grupo
Then sistema retorna MaxGroupsLimitException
```

---

## 7. Matriz de Trazabilidad

| RF | Resolver | Service | Repository | Entity |
|----|---------|---------|------------|--------|
| RF-EVT-01 | `EventQueryResolver` | `EventService` | `EventRepository` | `Event` |
| RF-EVT-06 | `EventMutationResolver` | `EventService` | `EventAttendeeRepository` | `EventAttendee` |
| RF-EVT-09 | `GroupMutationResolver` | `GroupService` | `StudyGroupRepository` | `StudyGroup` |
| RF-EVT-10 | `GroupMutationResolver` | `GroupService` | `GroupMembershipRepository` | `GroupMembership` |
| RF-EVT-11 | `GroupMutationResolver` | `GroupService` | `GroupMembershipRepository` | `GroupMembership` |
| RF-EVT-12 | `EventMutationResolver` | `EventService` | — | (validación en servicio) |

---

## 8. Dependencias

| Dependencia | Tipo |
|-------------|------|
| `Event`, `EventAttendee`, `StudyGroup`, `GroupMembership` | Entidades |
| `EventRepository`, `EventAttendeeRepository` | Repositorios |
| `StudyGroupRepository`, `GroupMembershipRepository` | Repositorios |
| `SlugService` | Servicio |
| `NotificationService` | Servicio (para notificar solicitudes) |

---

## 9. Priorización

### MVP
RF-EVT-01, RF-EVT-02, RF-EVT-03, RF-EVT-06, RF-EVT-07, RF-EVT-08, RF-EVT-09, RF-EVT-10, RF-EVT-12, RF-EVT-13

### Fase 2
RF-EVT-04, RF-EVT-05, RF-EVT-11

---

## 10. TODOs

| ID | Tarea | Prioridad | Esfuerzo | Notas |
|----|-------|-----------|----------|-------|
| TODO-EVT-01 | Migración Flyway: events + event_attendees + study_groups + group_memberships | Alta | 5h | V4__create_events_groups.sql |
| TODO-EVT-02 | Implementar SlugService con sufijo aleatorio (6 chars) | Alta | 3h | RandomStringUtils |
| TODO-EVT-03 | Validar fecha futura en createEvent | Alta | 2h | @Future o validación manual |
| TODO-EVT-04 | Unique constraints: (user_id, event_id), group_name, group_slug | Alta | 2h | DDL + índices |
| TODO-EVT-05 | Límite 10 grupos por estudiante en joinGroup | Alta | 3h | COUNT query |
| TODO-EVT-06 | Límite 50 miembros por grupo en joinGroup | Alta | 3h | COUNT query |
| TODO-EVT-07 | Tests de integración: RSVP con capacidad, grupos públicos/privados | Alta | 10h | GraphQLTestTemplate |
| TODO-EVT-08 | Tests unitarios: validación de límites, RSVP toggle | Alta | 6h | Mockito |
| TODO-EVT-09 | Chat en vivo dentro del grupo (WebSocket por grupo) | Media | 16h | /topic/group/{groupId} |
| TODO-EVT-10 | Notificación 24h antes del evento (scheduled job) | Media | 6h | @Scheduled + NotificationService |
| TODO-EVT-11 | Integración calendario (.ics export) | Baja | 5h | biweekly library |
| TODO-EVT-12 | Videollamada integrada Jitsi | Baja | 20h | iframe + Jitsi API |
| TODO-EVT-13 | Grupos recurrentes con schedule semanal | Baja | 10h | Nuevo campo recurrenceRule |

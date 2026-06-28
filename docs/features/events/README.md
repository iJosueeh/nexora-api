# Módulo: Eventos y Grupos de Estudio Temáticos

## 1. Objetivo del módulo

Permitir a los estudiantes crear salas virtuales de estudio y agendar eventos académicos (talleres, repasos, tutorías), facilitando la organización por intereses comunes y carreras.

## 2. Alcance

- **Incluye:** CRUD de eventos, RSVP con control de capacidad, CRUD de grupos de estudio, membresías con roles.
- **Incluye:** Categorización por carrera e intereses, generación de slugs para URLs públicas.
- **No incluye:** Chat en vivo dentro de grupos, videollamadas, integración con calendarios externos.

## 3. Lógica de negocio

- **Eventos:**
  - Cualquier estudiante autenticado puede crear un evento.
  - Capacidad máxima configurable (ilimitada si no se especifica).
  - RSVP único: un estudiante puede confirmar/cancelar asistencia.
  - Si el evento está lleno, se activa lista de espera (opcional).
  - Generación automática de slug a partir del título.
- **Grupos de estudio:**
  - Cualquier estudiante puede crear un grupo temático.
  - El creador es `OWNER`, puede asignar `MODERATOR` a otros miembros.
  - Unirse puede ser libre (cualquier estudiante) o por solicitud (según configuración del grupo).
  - Máximo 50 miembros por grupo (configurable por instancia).
  - Los grupos tienen un muro interno para compartir recursos y mensajes.

## 4. Validaciones

### Entrada

- `EventInput.title`: requerido, min 5, max 200.
- `EventInput.description`: opcional, max 2000.
- `EventInput.startDate`: requerido, debe ser futuro.
- `EventInput.endDate`: requerido, debe ser posterior a startDate.
- `EventInput.capacity`: opcional, entero positivo.
- `GroupInput.name`: requerido, min 3, max 100, único.

### Reglas de dominio

- RSVP único: constraint `(user_id, event_id)`.
- Slug único para eventos y grupos (generado automáticamente).
- El creador del evento/grupo no puede abandonarlo (debe transferir ownership o eliminar).
- Un estudiante no puede unirse a un grupo si ya está en 10 grupos (límite configurable).

## 5. Contratos API

### GraphQL

```graphql
type Query {
    events(cursor: String, limit: Int, filter: EventFilter): EventConnection!
    eventBySlug(slug: String!): Event
    myEvents(upcoming: Boolean): [Event!]!
    groups(cursor: String, limit: Int, filter: GroupFilter): GroupConnection!
    groupBySlug(slug: String!): StudyGroup
    myGroups: [StudyGroup!]!
}

type Mutation {
    createEvent(input: EventInput!): Event!
    updateEvent(id: UUID!, input: EventInput!): Event!
    deleteEvent(id: UUID!): Boolean!
    rsvpEvent(eventId: UUID!): RSVPPayload!
    createGroup(input: GroupInput!): StudyGroup!
    updateGroup(id: UUID!, input: GroupInput!): StudyGroup!
    deleteGroup(id: UUID!): Boolean!
    joinGroup(groupId: UUID!): GroupMembership!
    leaveGroup(groupId: UUID!): Boolean!
    approveMembership(membershipId: UUID!): GroupMembership!
}

type Event {
    id: UUID!
    slug: String!
    title: String!
    description: String
    startDate: DateTime!
    endDate: DateTime!
    location: String
    capacity: Int
    attendeesCount: Int!
    isFull: Boolean!
    isAttendingByMe: Boolean!
    category: EventCategory!
    organizer: User!
    createdAt: DateTime!
}

type StudyGroup {
    id: UUID!
    slug: String!
    name: String!
    description: String
    memberCount: Int!
    maxMembers: Int!
    isMember: Boolean!
    myRole: GroupRole
    category: String!
    createdAt: DateTime!
}

enum GroupRole { OWNER MODERATOR MEMBER }
```

### REST

- No aplica.

## 6. Persistencia

- Entidades: `Event`, `EventAttendee`, `StudyGroup`, `GroupMembership`.
- Repositorios: `EventRepository`, `EventAttendeeRepository`, `StudyGroupRepository`, `GroupMembershipRepository`.
- `EventAttendee`: unique constraint `(user_id, event_id)`.
- `GroupMembership`: unique constraint `(user_id, group_id)`.
- Slugs generados con `slugify` + sufijo aleatorio si hay conflicto.

## 7. Seguridad

- `events`, `eventBySlug`, `groups`, `groupBySlug` — público.
- `createEvent`, `createGroup`, `rsvpEvent`, `joinGroup` — autenticado.
- `updateEvent`, `deleteEvent` — solo organizador.
- `updateGroup`, `deleteGroup` — solo OWNER.
- `approveMembership` — OWNER o MODERATOR.

## 8. Errores y excepciones

| Escenario | Excepción | Código GraphQL |
|-----------|-----------|----------------|
| Evento lleno | `EventFullException` | BAD_REQUEST |
| RSVP duplicado (toggle) | — | OK (cancela asistencia) |
| Grupo con nombre duplicado | `DuplicateGroupNameException` | BAD_REQUEST |
| Límite de grupos alcanzado | `MaxGroupsLimitException` | BAD_REQUEST |
| No autorizado a editar | `AccessDeniedException` | FORBIDDEN |

## 9. Dependencias del módulo

- `CatalogService` — categorías por carrera
- `UserRepository` — validación de miembros
- `SlugService` — generación de slugs únicos
- `NotificationService` — notificar RSVP y solicitudes

## 10. Observabilidad

- Métrica: eventos creados por semana, asistencia promedio.
- Loggear creación de grupos y asignación de moderadores.
- Tasa de conversión: vistas de evento → RSVP.

## 11. Casos de prueba sugeridos

- Crear evento con capacidad genera slug único.
- RSVP exitoso incrementa attendeeCount.
- RSVP cuando evento lleno → error.
- Unirse a grupo libre agrega membresía automática.
- Solicitud a grupo privado queda pendiente.
- OWNER puede transferir moderación.
- Límite de 10 grupos por estudiante.

## 12. TODO / Pendientes

- Chat en vivo dentro del grupo (WebSocket).
- Recordatorio automático 24h antes del evento (notificación).
- Exportar evento a Google Calendar / Outlook (.ics).
- Videollamada integrada (Jitsi / Zoom API).
- Grupos de estudio recurrentes (ej: "Repaso semanal de Cálculo").

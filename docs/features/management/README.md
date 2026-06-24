# Módulo: Administración y Moderación

## 1. Objetivo del módulo

Espacio exclusivo para roles autorizados (Administrador u Oficial) que permite revisar estadísticas generales, desactivar cuentas, moderar contenido y destacar publicaciones institucionales.

## 2. Alcance

- **Incluye:** Dashboard con estadísticas, gestión de usuarios (ADMIN), moderación de contenido (ADMIN + OFFICIAL), auditoría de acciones.
- **Incluye:** Destacar publicaciones (pin), ocultar contenido inapropiado, desactivar/reactivar cuentas.
- **No incluye:** Configuración del sistema, logs de infraestructura, respaldos desde UI.

## 3. Lógica de negocio

- **Dashboard:**
  - Estadísticas en tiempo real: usuarios activos, publicaciones del día, eventos próximos, reportes pendientes.
  - Gráficos de actividad semanal/mensual.
  - Cache de estadísticas con refresco cada 5 minutos.
- **Usuarios (solo ADMIN):**
  - Listado paginado con búsqueda por nombre/email.
  - Desactivar: `isActive = false`, el usuario no puede iniciar sesión.
  - Cambio de rol: solo entre STUDENT y OFFICIAL (no puede auto-degradarse).
  - No se puede desactivar la propia cuenta ni a otros ADMIN.
- **Publicaciones (ADMIN + OFFICIAL):**
  - Ocultar (soft delete forzado): la publicación se marca como `hiddenByModerator`.
  - Destacar (pin): la publicación aparece al inicio del feed con indicador visual.
  - Límite de 3 publicaciones destacadas simultáneamente.
- **Auditoría:**
  - Cada acción de moderación se registra en `AuditLog`: adminId, action, targetType, targetId, timestamp.
  - Logs inmutables (solo inserción, sin modificación ni eliminación).

## 4. Validaciones

### Entrada

- `ModeratePostInput.action`: enum `HIDE | UNHIDE | PIN | UNPIN`.
- `UserManagementInput.userId`: UUID, no puede ser el propio ADMIN.
- `UserManagementInput.newRole`: enum `STUDENT | OFFICIAL` (no ADMIN).

### Reglas de dominio

- Un ADMIN no puede desactivar su propia cuenta ni cambiar su propio rol.
- Máximo 3 publicaciones destacadas simultáneamente.
- OFFICIAL no puede gestionar usuarios ni roles.
- Auditoría obligatoria para cada acción de moderación.

## 5. Contratos API

### GraphQL

```graphql
type Query {
    dashboardStats(period: DateRange): DashboardStats!
    adminUsers(cursor: String, limit: Int, search: String, filter: UserFilter): AdminUserConnection!
    adminPosts(cursor: String, limit: Int, filter: PostFilter): AdminPostConnection!
    auditLogs(cursor: String, limit: Int, action: AuditAction): AuditLogConnection!
}

type Mutation {
    moderatePost(id: UUID!, action: ModerateAction!): Post!
    deactivateUser(id: UUID!): Boolean!
    reactivateUser(id: UUID!): Boolean!
    changeUserRole(id: UUID!, newRole: UserRole!): User!
}

type DashboardStats {
    totalUsers: Int!
    activeUsers: Int!
    postsToday: Int!
    upcomingEvents: Int!
    pendingReports: Int!
    activityGraph: [ActivityPoint!]!
    topContributors: [User!]!
}

enum ModerateAction { HIDE UNHIDE PIN UNPIN }
enum UserRole { STUDENT OFFICIAL ADMIN }

type AuditLog {
    id: UUID!
    adminId: UUID!
    adminName: String!
    action: String!
    targetType: String!
    targetId: UUID!
    details: String
    createdAt: DateTime!
}
```

### REST

- No aplica.

## 6. Persistencia

- Entidades: `AuditLog` (tabla de auditoría inmutable).
- Repositorios: `AuditLogRepository`, `UserRepository`, `PostRepository`.
- `AuditLog`: solo inserción, sin update/delete. Índice por `(admin_id, created_at DESC)`.
- Dashboard stats: consultas agregadas sobre `users`, `posts`, `events` (con cache en memoria).

## 7. Seguridad

- Todos los endpoints requieren rol `ADMIN` u `OFFICIAL`.
- `deactivateUser`, `reactivateUser`, `changeUserRole` — solo `ADMIN`.
- `auditLogs` — solo `ADMIN`.

## 8. Errores y excepciones

| Escenario | Excepción | Código GraphQL |
|-----------|-----------|----------------|
| Auto-desactivación | `SelfActionNotAllowedException` | BAD_REQUEST |
| Desactivar otro ADMIN | `SelfActionNotAllowedException` | BAD_REQUEST |
| Máximo pins alcanzado | `MaxPinsLimitException` | BAD_REQUEST |
| OFFICIAL intenta gestión usuarios | `AccessDeniedException` | FORBIDDEN |
| Usuario no encontrado | `ResourceNotFoundException` | NOT_FOUND |

## 9. Dependencias del módulo

- `UserRepository`, `PostRepository`, `EventRepository`, `AuditLogRepository`
- `DashboardStatsService` — agregación y caché de estadísticas
- `NotificationService` — notificar al usuario si su cuenta es desactivada (opcional)

## 10. Observabilidad

- Todos los cambios de rol y desactivaciones se registran en AuditLog.
- Métrica: acciones de moderación por admin por día.
- Dashboard expone métricas en tiempo real.

## 11. Casos de prueba sugeridos

- ADMIN puede desactivar un estudiante.
- ADMIN no puede desactivar su propia cuenta.
- OFFICIAL no puede acceder a gestión de usuarios.
- Destacar publicación (pin) cuando hay < 3 pins.
- Destacar cuando ya hay 3 pins → error.
- Auditoría registra cada acción de moderación.
- Dashboard retorna stats correctos.

## 12. TODO / Pendientes

- Sistema de reportes de contenido por usuarios.
- Notificaciones masivas (anuncios desde el panel).
- Exportación de auditoría a CSV/PDF.
- Respaldos programados desde la UI.
- Historial de actividad por usuario (admin view).

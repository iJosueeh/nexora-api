# Requerimientos: Administración y Moderación

## 1. Visión General

API GraphQL para el panel de administración y moderación. Provee dashboard con estadísticas agregadas, gestión de usuarios (ADMIN), moderación de contenido (ADMIN + OFFICIAL), y auditoría inmutable de acciones.

---

## 2. Requerimientos Funcionales

| ID | Requerimiento | Prioridad | Estimación |
|----|--------------|-----------|------------|
| RF-MGT-01 | Exponer query `dashboardStats(period)` con estadísticas agregadas | Alta | 10 pts |
| RF-MGT-02 | Exponer query `adminUsers(cursor, limit, search, filter)` con filtros | Alta | 8 pts |
| RF-MGT-03 | Exponer mutation `deactivateUser(id)` y `reactivateUser(id)` | Alta | 5 pts |
| RF-MGT-04 | Exponer mutation `changeUserRole(id, newRole)` (solo STUDENT↔OFFICIAL) | Alta | 5 pts |
| RF-MGT-05 | Exponer query `adminPosts(cursor, limit, filter)` para moderación | Alta | 8 pts |
| RF-MGT-06 | Exponer mutation `moderatePost(id, action)` con HIDE/UNHIDE/PIN/UNPIN | Alta | 8 pts |
| RF-MGT-07 | Exponer query `auditLogs(cursor, limit, action)` para auditoría | Alta | 5 pts |
| RF-MGT-08 | Validar auto-exclusión: ADMIN no puede desactivarse a sí mismo | Alta | 3 pts |
| RF-MGT-09 | Validar límite de 3 publicaciones destacadas simultáneas | Alta | 3 pts |
| RF-MGT-10 | Validar que OFFICIAL no acceda a gestión de usuarios | Alta | 3 pts |
| RF-MGT-11 | Registrar cada acción de moderación en AuditLog | Alta | 5 pts |

**Total estimado:** 63 pts

---

## 3. Requerimientos No Funcionales

| ID | Requerimiento | Tipo | Métrica |
|----|--------------|------|---------|
| RNF-MGT-01 | Dashboard < 2s con stats cacheados 5 min | Rendimiento | 2s |
| RNF-MGT-02 | Listado de usuarios < 500ms | Rendimiento | 500ms |
| RNF-MGT-03 | AuditLog solo inserción (no UPDATE, no DELETE) | Integridad | — |
| RNF-MGT-04 | Roles granular: ADMIN todo, OFFICIAL solo contenido | Seguridad | — |
| RNF-MGT-05 | Cache de dashboard en Redis o Caffeine | Rendimiento | — |

---

## 4. Reglas de Negocio

| ID | Regla | Excepción |
|----|-------|-----------|
| RN-MGT-01 | Auto-exclusión: ADMIN ≠ target | `SelfActionNotAllowedException` |
| RN-MGT-02 | No desactivar otro ADMIN | `SelfActionNotAllowedException` |
| RN-MGT-03 | Máximo 3 pins | `MaxPinsLimitException` |
| RN-MGT-04 | OFFICIAL no gestiona usuarios | `AccessDeniedException` |
| RN-MGT-05 | Auditoría inmutable (INSERT only) | — |
| RN-MGT-06 | Roles mutables: STUDENT ↔ OFFICIAL (no ADMIN) | `ValidationException` |

---

## 5. Casos de Uso

### CU-MGT-01: Dashboard stats
- **Query:** `dashboardStats(period: { start, end })`
- **Cálculo:**
  - `totalUsers`: COUNT de users
  - `activeUsers`: COUNT WHERE isActive = true
  - `postsToday`: COUNT WHERE createdAt >= today
  - `upcomingEvents`: COUNT WHERE startDate > now()
  - `pendingReports`: COUNT de reports pendientes
  - `activityGraph`: [{ date, posts, users }] por día en el período
- **Cache:** Caffeine, TTL 5 min, invalidar al crear post/usuario

### CU-MGT-02: Moderar post (ocultar)
1. `moderatePost(id, action: HIDE)`
2. Sistema verifica rol ADMIN u OFFICIAL (Spring Security)
3. Sistema marca `post.hiddenByModerator = true`
4. Sistema registra en `AuditLog`: adminId, action="HIDE", targetType="POST", targetId
5. Post oculto de feeds públicos, visible solo para admin

### CU-MGT-03: Desactivar usuario con validaciones
1. `deactivateUser(id)`
2. Sistema verifica `currentUser.id != id` (no auto-desactivación)
3. Sistema verifica `target.role != ADMIN` (no desactivar otro admin)
4. Sistema verifica `currentUser.role == ADMIN` (solo admin)
5. Sistema marca `user.isActive = false`
6. Sistema registra en `AuditLog`

---

## 6. Criterios de Aceptación

### CA-MGT-01: Dashboard con datos reales
```
Given 100 usuarios, 50 activos, 10 posts hoy, 3 eventos próximos
When se consulta dashboardStats(period: LAST_7_DAYS)
Then retorna totalUsers=100, activeUsers=50, postsToday=10, upcomingEvents=3
And activityGraph con 7 puntos de datos
```

### CA-MGT-02: PIN con límite
```
Given 3 posts con isPinned=true
When se ejecuta moderatePost(postId, PIN)
Then error "Maximum pinned posts reached (3)"
```

### CA-MGT-03: Auditoría inmutable
```
Given un AuditLog registrado
When se intenta UPDATE o DELETE en la tabla audit_logs
Then la BD rechaza la operación (sin permisos o trigger)
```

---

## 7. Matriz de Trazabilidad

| RF | Resolver | Service | Repository | Entity |
|----|---------|---------|------------|--------|
| RF-MGT-01 | `AdminQueryResolver` | `DashboardService` | `UserRepository`, `PostRepository`, `EventRepository` | — |
| RF-MGT-02 | `AdminQueryResolver` | `UserAdminService` | `UserRepository` | `User` |
| RF-MGT-03 | `AdminMutationResolver` | `UserAdminService` | `UserRepository` | `User` |
| RF-MGT-06 | `AdminMutationResolver` | `PostModerationService` | `PostRepository` | `Post` |
| RF-MGT-07 | `AdminQueryResolver` | `AuditService` | `AuditLogRepository` | `AuditLog` |
| RF-MGT-08 | `AdminMutationResolver` | `UserAdminService` | — | — |

---

## 8. Dependencias

| Dependencia | Tipo |
|-------------|------|
| `UserRepository`, `PostRepository`, `EventRepository` | Repositorios |
| `AuditLog` (entidad inmutable) | Entidad |
| `DashboardService` | Servicio |
| `CacheManager` (Caffeine/Redis) | Infraestructura |
| Spring Security (role-based) | Seguridad |

---

## 9. Priorización

### MVP
RF-MGT-01, RF-MGT-02, RF-MGT-03, RF-MGT-05, RF-MGT-06, RF-MGT-08, RF-MGT-09, RF-MGT-10, RF-MGT-11

### Fase 2
RF-MGT-04, RF-MGT-07

---

## 10. TODOs

| ID | Tarea | Prioridad | Esfuerzo | Notas |
|----|-------|-----------|----------|-------|
| TODO-MGT-01 | Migración Flyway: audit_logs | Alta | 3h | V6__create_audit_logs.sql |
| TODO-MGT-02 | Implementar DashboardService con consultas agregadas | Alta | 8h | JPQL COUNT + GROUP BY + cache |
| TODO-MGT-03 | Cache de dashboard con Caffeine (TTL 5 min) | Alta | 3h | @Cacheable + @CacheEvict |
| TODO-MGT-04 | Validación de auto-exclusión y targeting ADMIN | Alta | 4h | SecurityUtil.getCurrentUser() |
| TODO-MGT-05 | Límite de 3 pins con COUNT query + validación | Alta | 3h | postRepository.countByIsPinnedTrue() |
| TODO-MGT-06 | AuditLog: trigger o constraint para evitar UPDATE/DELETE | Alta | 3h | PostgreSQL event trigger o permisos |
| TODO-MGT-07 | Tests de integración: dashboard stats, moderación, auditoría | Alta | 12h | GraphQLTestTemplate |
| TODO-MGT-08 | Tests unitarios: validaciones auto-exclusión, límite pins | Alta | 6h | Mockito |
| TODO-MGT-09 | Sistema de reportes de contenido por usuarios | Media | 10h | Nueva entidad ContentReport |
| TODO-MGT-10 | Exportación de auditoría a CSV desde query | Media | 5h | CSVWriter |
| TODO-MGT-11 | Historial de actividad por usuario (admin view query) | Baja | 6h | Query adminUserActivityLog(userId) |

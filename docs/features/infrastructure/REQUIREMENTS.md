# Requerimientos: Persistencia e Infraestructura en la Nube

## 1. Visión General

Configuración de la infraestructura backend: base de datos PostgreSQL 16 gestionada por Flyway, almacenamiento S3-compatible, WebSocket STOMP, seguridad perimetral, Docker y observabilidad.

---

## 2. Requerimientos Funcionales

| ID | Requerimiento | Prioridad | Estimación |
|----|--------------|-----------|------------|
| RF-INF-01 | Conectar a PostgreSQL 16 con pool HikariCP (max 20) | Alta | 5 pts |
| RF-INF-02 | Gestionar esquema de BD con Flyway (ddl-auto: none) | Alta | 5 pts |
| RF-INF-03 | Almacenar archivos en buckets S3 separados por tipo | Alta | 10 pts |
| RF-INF-04 | Generar URLs prefirmadas con expiración configurable | Alta | 5 pts |
| RF-INF-05 | Configurar WebSocket STOMP autenticado con JWT | Alta | 8 pts |
| RF-INF-06 | Exponer health check en `/api/health` | Alta | 3 pts |
| RF-INF-07 | Configurar CORS para origen del frontend | Alta | 2 pts |
| RF-INF-08 | Configurar headers de seguridad (CSP, HSTS, X-Frame-Options) | Alta | 3 pts |
| RF-INF-09 | Proveer Dockerfile y docker-compose para entorno multi-servicio | Alta | 8 pts |
| RF-INF-10 | Programa respaldo diario de BD con pg_dump | Media | 5 pts |
| RF-INF-11 | Exponer métricas via Spring Boot Actuator | Media | 5 pts |
| RF-INF-12 | Versionar archivos en S3 (protección contra borrado accidental) | Media | 5 pts |

**Total estimado:** 64 pts

---

## 3. Requerimientos No Funcionales

| ID | Requerimiento | Tipo | Métrica |
|----|--------------|------|---------|
| RNF-INF-01 | Pool HikariCP: max 20, min 5, timeout 30s | Rendimiento | — |
| RNF-INF-02 | Latencia conexión BD < 100ms (p95) | Rendimiento | 100ms |
| RNF-INF-03 | Upload S3 < 3s para < 5MB | Rendimiento | 3s |
| RNF-INF-04 | URLs prefirmadas expiran en 15 min (configurable) | Seguridad | 15 min |
| RNF-INF-05 | Reconexión WS backoff: 3s, 6s, 12s, max 30s | Confiabilidad | — |
| RNF-INF-06 | Heartbeat WS cada 30s | Confiabilidad | 30s |
| RNF-INF-07 | Respaldo diario, retención 7d/4s/12m | Mantenimiento | — |
| RNF-INF-08 | CORS whitelist configurable | Seguridad | — |
| RNF-INF-09 | S3 versioning habilitado | Recuperación | — |

---

## 4. Componentes de Infraestructura

### Base de Datos

| Propiedad | Valor |
|-----------|-------|
| Motor | PostgreSQL 16 |
| Driver | org.postgresql.Driver |
| Pool | HikariCP (max 20, min 5) |
| Migraciones | Flyway (locations: classpath:db/migration) |
| DDL | none (solo Flyway) |
| Naming | snake_case, plural |
| PK | UUID (generado por aplicación) |
| Auditoría | `BaseEntity`: createdAt, updatedAt |
| | `AuditableBaseEntity`: + createdBy, updatedBy |

### Storage S3

| Bucket | Propósito | TTL prefirmado |
|--------|-----------|----------------|
| `nexora-avatars` | Avatares de perfil | 15 min |
| `nexora-covers` | Portadas de perfil | 15 min |
| `nexora-resources` | Recursos académicos | 15 min |
| `nexora-posts` | Multimedia de posts | 15 min |
| `nexora-temp` | Archivos temporales | 5 min |

**Configuración:**
```yaml
app:
  storage:
    endpoint: ${STORAGE_ENDPOINT}
    region: ${STORAGE_REGION:us-east-1}
    access-key: ${STORAGE_ACCESS_KEY}
    secret-key: ${STORAGE_SECRET_KEY}
    buckets:
      avatars: nexora-avatars
      covers: nexora-covers
      resources: nexora-resources
      posts: nexora-posts
    presigned-url-expiry: 15m
```

### WebSocket STOMP

| Propiedad | Valor |
|-----------|-------|
| Endpoint | `/ws` |
| Protocolo | STOMP sobre WebSocket |
| Fallback | SockJS |
| Auth | JWT en query param `?token=` |
| Heartbeat | 30s |
| Max message | 8KB |
| Canales | `/topic/notifications/{userId}` |

### Docker Compose

| Servicio | Puerto | Depende de |
|----------|--------|------------|
| `db` (postgres:16-alpine) | 5432 | — |
| `core` (Spring Boot) | 8080 | db |
| `app` (nginx) | 4200 | core |

---

## 5. Seguridad de Infraestructura

| Capa | Medida |
|------|--------|
| Transporte | HTTPS obligatorio en producción |
| API | JWT HMAC-SHA256 |
| Storage | URLs prefirmadas 15 min |
| CORS | Solo origen frontend configurado |
| Headers | CSP, HSTS, X-Frame-Options: DENY, X-Content-Type-Options: nosniff |
| BD | Usuario con permisos mínimos (solo DML en schema propio) |
| Contenedores | No root, read-only filesystem donde sea posible |

---

## 6. Observabilidad

### Health Checks
- `GET /api/health` → `{ status: "UP", database: "UP", storage: "UP" }`
- `GET /actuator/health` → Spring Boot Actuator (detalles de BD, disco, ping)

### Métricas (Actuator + Micrometer)
- Tiempo de respuesta por endpoint (p50, p95, p99)
- Tasa de errores por endpoint
- Conexiones activas BD (HikariCP)
- Conexiones WS activas
- Almacenamiento usado por bucket S3
- Tokens JWT emitidos vs. refrescados

### Logs
- Formato JSON estructurado en producción
- Niveles: ERROR (producción), DEBUG (desarrollo)
- No loggear: contraseñas, tokens, contenidos de usuarios

---

## 7. Respaldo y Recuperación

| Estrategia | Frecuencia | Retención |
|-----------|-----------|-----------|
| pg_dump completo | Diario (03:00 UTC) | 7 días (diarios), 4 semanas (semanales), 12 meses (mensuales) |
| S3 versioning | Continuo | Indefinido (protege contra borrado) |
| Prueba de restauración | Mensual | Verificar integridad del respaldo |

---

## 8. Priorización

### MVP (Fase 1)
RF-INF-01, RF-INF-02, RF-INF-03, RF-INF-04, RF-INF-05, RF-INF-06, RF-INF-07, RF-INF-08, RF-INF-09

### Fase 2
RF-INF-10, RF-INF-11, RF-INF-12

---

## 9. TODOs

| ID | Tarea | Prioridad | Esfuerzo | Notas |
|----|-------|-----------|----------|-------|
| TODO-INF-01 | Script de respaldo automático (pg_dump + cron) | Media | 4h | Docker cron container |
| TODO-INF-02 | Implementar health check personalizado (BD + S3 + WS) | Alta | 4h | HealthIndicator |
| TODO-INF-03 | Configurar Spring Boot Actuator con métricas expuestas | Media | 5h | /actuator/prometheus |
| TODO-INF-04 | Dashboard Grafana + Prometheus | Baja | 20h | docker-compose extra |
| TODO-INF-05 | Migrar a AWS S3 + CloudFront CDN | Baja | 16h | Escalamiento |
| TODO-INF-06 | Agregar Redis para caché (trending, sesiones, rate limiting) | Media | 10h | spring-boot-starter-data-redis |
| TODO-INF-07 | Read replicas PostgreSQL para queries pesadas | Baja | 20h | Arquitectura multi-DB |
| TODO-INF-08 | S3 bucket versioning + lifecycle policy | Media | 3h | AWS Console o SDK |
| TODO-INF-09 | Compresión y optimización de imágenes server-side | Media | 6h | Thumbnailator |
| TODO-INF-10 | Prueba mensual de restauración de respaldo | Media | 2h/mes | Documentar procedimiento |
| TODO-INF-11 | Documentar runbook de despliegue y recuperación ante desastres | Alta | 8h | DRP document |
| TODO-INF-12 | Configurar alertas de monitoreo (Discord/Slack webhook) | Media | 4h | Webhook genérico |

# Módulo: Persistencia e Infraestructura en la Nube

## 1. Objetivo del módulo

Configuración de la base de datos relacional (PostgreSQL) para resguardar la información del sistema y un sistema de almacenamiento remoto (S3-compatible) para la gestión de archivos multimedia y documentos de estudio.

## 2. Base de Datos (PostgreSQL)

### Gestión de esquemas

- **Herramienta:** Flyway (migraciones en `src/main/resources/db/migration/`).
- **Convención:** Naming `V{version}__{description}.sql`.
- **DDL automático:** `spring.jpa.hibernate.ddl-auto: none` — Hibernate no crea tablas.
- **Naming:** Tablas en plural con `snake_case` (ej: `research_papers`, `student_profiles`).

### Configuración (`application.yml`)

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration
```

### Entidades base

- `BaseEntity`: UUID como PK, `createdAt`, `updatedAt` (auditoría automática).
- `AuditableBaseEntity`: extiende `BaseEntity` + `createdBy`, `updatedBy`.

### Conexiones

- Pool de conexiones: HikariCP (configuración por defecto optimizada).
- Timeout: 30s connection, 500ms idle.

## 3. Almacenamiento Remoto (S3-compatible)

### Proveedores soportados

| Proveedor | Tipo | Uso |
|-----------|------|-----|
| Supabase Storage | S3-compatible | Desarrollo y producción inicial |
| AWS S3 | Nativo | Escalamiento futuro |
| MinIO | S3-compatible | Desarrollo local (Docker) |

### Configuración

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

### Flujo de archivos

1. Cliente sube archivo → servidor valida formato/tamaño.
2. Servidor procesa (redimensiona si es imagen) y sube al bucket correspondiente.
3. Se almacena la URL pública o prefirmada en la entidad.
4. Descarga: se genera URL prefirmada con expiración de 15 minutos.

### Límites

- Avatar: 2MB, formatos JPG, PNG, WEBP.
- Portada: 5MB, formatos JPG, PNG, WEBP.
- Recursos académicos: 20MB, formatos PDF, EPUB, MD, PPTX, DOCX.
- Posts multimedia: 10MB total por post.

## 4. Conexiones en Vivo (WebSocket)

### Stack

- **Protocolo:** STOMP sobre WebSocket (con SockJS fallback).
- **Configuración:** `WebSocketConfig` en `infrastructure/config/`.
- **Endpoint:** `/ws`, autenticado con JWT en query param `?token=`.

### Canales

| Canal | Propósito |
|-------|-----------|
| `/topic/notifications/{userId}` | Notificaciones en tiempo real |
| `/topic/group/{groupId}` | Chat y actividad de grupo (futuro) |

### Lifecycle

- Conexión se establece al autenticarse el usuario.
- Heartbeat cada 30s para mantener sesión activa.
- Reconexión automática con backoff (3s, 6s, 12s, max 30s).

## 5. Infraestructura Docker

### `docker-compose.yml` (raíz del workspace)

```yaml
services:
  core:
    build: ./nexora-core
    ports:
      - "8080:8080"
    environment:
      - DB_URL=jdbc:postgresql://db:5432/nexora
      - DB_USERNAME=nexora
      - DB_PASSWORD=${DB_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - STORAGE_ENDPOINT=${STORAGE_ENDPOINT}
    depends_on:
      - db

  app:
    build: ./nexora-app
    ports:
      - "4200:80"
    environment:
      - API_BASE_URL=http://core:8080
      - GRAPHQL_URL=http://core:8080/graphql
      - SUPABASE_URL=${SUPABASE_URL}
      - SUPABASE_ANON_KEY=${SUPABASE_ANON_KEY}

  db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: nexora
      POSTGRES_USER: nexora
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - pgdata:/var/lib/postgresql/data

volumes:
  pgdata:
```

## 6. Seguridad de Infraestructura

- JWT firmado con HMAC-SHA256 (secreto configurable).
- URLs prefirmadas con expiración de 15 minutos.
- CORS configurado para origen del frontend.
- Headers de seguridad: CSP, X-Frame-Options, X-Content-Type-Options, Strict-Transport-Security.
- Rate limiting en endpoints críticos (login, registro).

## 7. Observabilidad

### Logs

- Formato JSON estructurado en producción.
- Niveles: ERROR (producción), DEBUG (desarrollo).
- Loggear: intentos de autenticación, subidas de archivos, errores de conexión DB.

### Métricas sugeridas

- Tiempo de respuesta de queries GraphQL (p50, p95, p99).
- Tasa de errores por endpoint.
- Conexiones activas de WebSocket.
- Tamaño de almacenamiento utilizado por bucket.
- Tiempo de generación de URLs prefirmadas.

### Health checks

- `/api/health` — estado de la aplicación.
- `/actuator/health` — Spring Boot Actuator (DB, disco, conexiones).

## 8. Respaldo y Recuperación

- Respaldo diario de PostgreSQL (pg_dump via cron job).
- Política de retención: 7 días (diario), 4 semanas (semanal), 12 meses (mensual).
- Archivos en S3 con versioning habilitado (recuperación ante borrado accidental).
- Prueba de restauración mensual programada.

## 9. TODO / Pendientes

- Implementar CDN para distribución de archivos estáticos.
- Migrar a AWS S3 + CloudFront para escalamiento.
- Agregar Redis para caché de trending y sesiones.
- Implementar read replicas de PostgreSQL para consultas pesadas.
- Dashboard de monitoreo (Grafana + Prometheus).

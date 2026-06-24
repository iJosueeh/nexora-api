# Requerimientos: Autenticación y Seguridad (REST + JWT)

## 1. Visión General

Módulo de acceso seguro que valida identidades estudiantiles mediante JWT, coordina el registro inicial con verificación de dominio institucional y gestiona el completado de datos con catálogos de la institución.

---

## 2. Requerimientos Funcionales

| ID | Requerimiento | Prioridad | Estimación |
|----|--------------|-----------|------------|
| RF-AUTH-01 | El sistema debe exponer `POST /api/auth/register` para registro con email institucional | Alta | 8 pts |
| RF-AUTH-02 | El sistema debe validar dominio del email contra whitelist configurable | Alta | 3 pts |
| RF-AUTH-03 | El sistema debe rechazar registro si el email ya existe (unique) | Alta | 2 pts |
| RF-AUTH-04 | El sistema debe exponer `POST /api/auth/login` para inicio de sesión | Alta | 5 pts |
| RF-AUTH-05 | El sistema debe generar JWT access token firmado (HMAC-SHA256) al autenticar | Alta | 5 pts |
| RF-AUTH-06 | El sistema debe exponer `POST /api/auth/refresh` para rotar refresh token | Alta | 8 pts |
| RF-AUTH-07 | El sistema debe exponer `GET /api/auth/me` para datos del usuario autenticado | Alta | 3 pts |
| RF-AUTH-08 | El sistema debe exponer `PUT /api/auth/onboarding` para completar datos institucionales | Alta | 8 pts |
| RF-AUTH-09 | El sistema debe marcar cuenta como `pending-onboarding` si no completa catálogo en 7 días | Media | 5 pts |
| RF-AUTH-10 | El sistema debe exponer `POST /api/auth/forgot-password` y `POST /api/auth/reset-password` | Media | 10 pts |
| RF-AUTH-11 | El sistema debe hashear contraseñas con BCrypt (costo >= 10) antes de persistir | Alta | 3 pts |
| RF-AUTH-12 | El sistema debe verificar `isActive=true` antes de permitir login | Alta | 2 pts |
| RF-AUTH-13 | El sistema debe exponer log de intentos de autenticación para auditoría | Media | 5 pts |

**Total estimado:** 67 pts

---

## 3. Requerimientos No Funcionales

| ID | Requerimiento | Tipo | Métrica |
|----|--------------|------|---------|
| RNF-AUTH-01 | BCrypt con costo >= 10 para hasheo de contraseñas | Seguridad | — |
| RNF-AUTH-02 | JWT access token expira en 24h, refresh en 7 días | Seguridad | — |
| RNF-AUTH-03 | Rate limiting: 5 intentos/min por IP en login | Seguridad | 5 req/min |
| RNF-AUTH-04 | Tiempo de respuesta login < 1s (p95) | Rendimiento | 1s |
| RNF-AUTH-05 | Tiempo de respuesta registro < 2s (p95) | Rendimiento | 2s |
| RNF-AUTH-06 | Soporte para 100 registros simultáneos | Escalabilidad | 100 concurrentes |
| RNF-AUTH-07 | JWT firmado con secreto configurable via `JWT_SECRET` | Seguridad | — |
| RNF-AUTH-08 | Claims mínimos: `sub` (email), `role`, `userId`, `iat`, `exp` | Seguridad | — |
| RNF-AUTH-09 | No loggear contraseñas ni tokens en texto plano | Seguridad | — |
| RNF-AUTH-10 | Dominio institucional configurable por `app.auth.allowed-domains` | Configurabilidad | — |

---

## 4. Historias de Usuario

### Épica: Registro y Primer Acceso

**HU-AUTH-01:** Como estudiante, quiero registrarme con mi email universitario para acceder a la plataforma.
- Criterios: email @universidad.edu, contraseña segura, aceptar términos.

**HU-AUTH-02:** Como estudiante nuevo, quiero completar mi carrera y semestre desde un catálogo para personalizar mi experiencia.
- Criterios: catálogo precargado, selección obligatoria, guardar perfil.

### Épica: Autenticación y Sesión

**HU-AUTH-03:** Como estudiante, quiero iniciar sesión de forma segura para acceder a mi cuenta.
- Criterios: credenciales válidas, sesión persistente, redirección post-login.

**HU-AUTH-04:** Como estudiante, quiero que mi sesión expire automáticamente por seguridad.
- Criterios: token expira, redirección a login, sin pérdida de datos no guardados.

### Épica: Administración de Acceso

**HU-AUTH-05:** Como administrador, quiero configurar los dominios de email permitidos para controlar el acceso institucional.
- Criterios: whitelist configurable, validación en registro.

**HU-AUTH-06:** Como administrador, quiero ver intentos de login fallidos para detectar actividad sospechosa.
- Criterios: log de auditoría, alerta por umbral de intentos.

---

## 5. Reglas de Negocio

| ID | Regla | Descripción | Excepción |
|----|-------|-------------|-----------|
| RN-AUTH-01 | Dominio institucional | Solo emails con dominio en whitelist pueden registrar | `InvalidEmailDomainException` |
| RN-AUTH-02 | Email único | No pueden existir dos cuentas con el mismo email | `EmailAlreadyExistsException` |
| RN-AUTH-03 | Password hasheado | BCrypt con salt aleatorio, mínimo 10 rounds | — |
| RN-AUTH-04 | JWT integridad | Firma HMAC-SHA256, validación en cada request | `ExpiredJwtException`, `SignatureException` |
| RN-AUTH-05 | Onboarding forzado | Estudiante debe completar catálogo antes de acceder a funcionalidades core | `AccessDeniedException` |
| RN-AUTH-06 | Auto-exclusión | ADMIN no puede desactivar su propia cuenta | `SelfActionNotAllowedException` |
| RN-AUTH-07 | Sesión stateless | No se persiste sesión en servidor (solo JWT) | — |

---

## 6. Casos de Uso Principales

### CU-AUTH-01: Registrar estudiante
- **Actor:** Estudiante no registrado
- **Precondición:** Email con dominio institucional válido, password cumple política
- **Flujo principal:**
  1. Sistema recibe `RegisterRequest` (email, password, fullName)
  2. Sistema valida formato email y dominio en whitelist
  3. Sistema verifica `email` no existe en BD
  4. Sistema hashea password con BCrypt
  5. Sistema persiste `User` con rol `STUDENT`, `isActive=true`
  6. Sistema genera access JWT + refresh token
  7. Sistema retorna `AuthResponse` con 201
- **Postcondición:** Usuario persistido, tokens generados
- **Flujo alternativo A1:** Email duplicado → `EmailAlreadyExistsException`, 400
- **Flujo alternativo A2:** Dominio no permitido → `InvalidEmailDomainException`, 400

### CU-AUTH-02: Iniciar sesión
- **Actor:** Estudiante registrado
- **Precondición:** Cuenta activa
- **Flujo principal:**
  1. Sistema recibe `LoginRequest` (email, password)
  2. Sistema autentica via `AuthenticationManager`
  3. Sistema verifica `isActive=true`
  4. Sistema genera access JWT + refresh token
  5. Sistema retorna `AuthResponse` con 200
- **Postcondición:** Tokens generados, sesión iniciada
- **Flujo alternativo A1:** Credenciales inválidas → `BadCredentialsException`, 401
- **Flujo alternativo A2:** Cuenta desactivada → 401 con mensaje específico

### CU-AUTH-03: Refrescar token
- **Actor:** Estudiante autenticado
- **Precondición:** Refresh token válido y no expirado
- **Flujo principal:**
  1. Sistema recibe refresh token
  2. Sistema valida firma y expiración
  3. Sistema verifica que el usuario aún está activo
  4. Sistema invalida refresh token anterior (rotation)
  5. Sistema genera nuevo access JWT + nuevo refresh token
  6. Sistema retorna 200 con nuevos tokens
- **Postcondición:** Tokens rotados, anterior invalidado

### CU-AUTH-04: Completar onboarding
- **Actor:** Estudiante recién registrado
- **Precondición:** `pending-onboarding=true`, usuario autenticado
- **Flujo principal:**
  1. Sistema recibe `OnboardingRequest` (careerId, facultyId, semester)
  2. Sistema valida IDs contra `InstitutionalCatalog`
  3. Sistema crea/actualiza `StudentProfile`
  4. Sistema marca `pending-onboarding=false`
  5. Sistema retorna `UserProfile` con 200
- **Postcondición:** `StudentProfile` persistido, acceso completo

---

## 7. Criterios de Aceptación

### CA-AUTH-01: Registro con dominio válido
```
Given un email con dominio @universidad.edu en whitelist
And una contraseña que cumple políticas de seguridad
When se envía POST /api/auth/register con datos válidos
Then el sistema retorna 201 con JWT access y refresh tokens
And el usuario queda autenticado
```

### CA-AUTH-02: Registro con dominio no permitido
```
Given un email con dominio @gmail.com
When se envía POST /api/auth/register
Then el sistema retorna 400 "Email domain not allowed"
```

### CA-AUTH-03: Login exitoso
```
Given un usuario registrado y activo
When se envía POST /api/auth/login con credenciales correctas
Then el sistema retorna 200 con JWT access token (24h) y refresh token (7d)
And el token contiene claims: sub, role, userId
```

### CA-AUTH-04: Rate limit excedido
```
Given una IP específica
When se realizan 6 intentos fallidos en 60 segundos
Then el sistema retorna 429 Too Many Requests
And bloquea la IP por 60 segundos
```

---

## 8. Matriz de Trazabilidad

| Requerimiento | Controlador | Servicio | Repositorio | Entidad |
|--------------|-------------|----------|-------------|---------|
| RF-AUTH-01 | `AuthController` | `AuthService` | `UserRepository` | `User` |
| RF-AUTH-02 | `AuthController` | `EmailDomainValidator` | — | — |
| RF-AUTH-04 | `AuthController` | `AuthenticationManager` | `UserRepository` | `User` |
| RF-AUTH-05 | — | `JwtService` | — | — |
| RF-AUTH-06 | `AuthController` | `JwtService` | `RefreshTokenRepository` | `RefreshToken` |
| RF-AUTH-07 | `AuthController` | `UserService` | `UserRepository` | `User` |
| RF-AUTH-08 | `AuthController` | `StudentProfileService` | `StudentProfileRepository` | `StudentProfile` |
| RF-AUTH-11 | — | `AuthService` | — | — |

---

## 9. Dependencias y Restricciones

| Dependencia | Tipo | Versión | Nota |
|-------------|------|---------|------|
| `spring-boot-starter-security` | Librería | 3.4.x | Seguridad base |
| `spring-boot-starter-web` | Librería | 3.4.x | REST controllers |
| `jjwt-api` / `jjwt-impl` / `jjwt-jackson` | Librería | 0.12.x | JWT generation |
| `commons-validator` | Librería | 1.9.x | Email validation |
| `User` entity | Entidad | — | Dependencia core |
| `StudentProfile` entity | Entidad | — | Perfil de estudiante |
| `InstitutionalCatalog` entity | Entidad | — | Catálogo de carreras |
| PostgreSQL 16 | BD | 16.x | Persistencia |

### Restricciones
- No usar `spring-security-oauth2-resource-server` (JWT propio)
- No almacenar JWT en localStorage (usar cookies HttpOnly o memory)
- Toda comunicación debe ser HTTPS en producción

---

## 10. Priorización (MVP vs Post-MVP)

### MVP — Fase 1 (Sprint 1-2)
- RF-AUTH-01: Registro
- RF-AUTH-02: Validación dominio
- RF-AUTH-03: Email único
- RF-AUTH-04: Login
- RF-AUTH-05: JWT access token
- RF-AUTH-07: Endpoint /me
- RF-AUTH-08: Onboarding
- RF-AUTH-11: BCrypt hashing
- RF-AUTH-12: Verificar isActive

### Fase 2 (Sprint 3-4)
- RF-AUTH-06: Refresh token rotation
- RF-AUTH-09: Pending-onboarding timeout
- RF-AUTH-10: Forgot/reset password
- RF-AUTH-13: Audit log de autenticación

### Fase 3 (Post-MVP)
- SSO / OAuth2 social login
- Verificación email con OTP
- 2FA (TOTP)
- Integración LDAP institucional
- WebAuthn / passkeys

---

## 11. TODOs y Pendientes Detallados

| ID | Tarea | Prioridad | Esfuerzo | Dependencia | Notas |
|----|-------|-----------|----------|-------------|-------|
| TODO-AUTH-01 | Implementar refresh token rotation con JWT ID (jti) | Alta | 8h | JwtService | Usar tabla refresh_tokens |
| TODO-AUTH-02 | Rate limiting con Bucket4j o Spring filter | Alta | 6h | — | 5 intentos/min por IP |
| TODO-AUTH-03 | Endpoint forgot-password (generar token + email) | Media | 8h | MailService | Token expira 1h |
| TODO-AUTH-04 | Endpoint reset-password (validar token + nuevo password) | Media | 5h | — | Invalidar token tras uso |
| TODO-AUTH-05 | Scheduled job para marcar pending-onboarding a los 7 días | Media | 4h | TaskScheduler | @Scheduled(cron = "0 0 2 * * ?") |
| TODO-AUTH-06 | Email de bienvenida post-registro | Media | 3h | MailService | Template HTML |
| TODO-AUTH-07 | Tabla de auditoría de intentos de login | Media | 6h | AuditLogRepository | IP, email, timestamp, éxito/fallo |
| TODO-AUTH-08 | Logout con blacklist de tokens (Redis) | Baja | 8h | Redis | Invalidar antes de expiración |
| TODO-AUTH-09 | Integración SSO SAML/OIDC | Baja | 40h | — | Futuro roadmap |
| TODO-AUTH-10 | Tests de integración: registro → login → refresh → onboarding | Alta | 8h | — | Cobertura de flujo completo |
| TODO-AUTH-11 | Tests unitarios: JwtService, EmailDomainValidator | Alta | 4h | — | Edge cases |
| TODO-AUTH-12 | Documentar política de contraseñas en README | Baja | 2h | — | Wiki/securit |

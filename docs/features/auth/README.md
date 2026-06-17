# Módulo: Autenticación y Seguridad (REST + JWT)

## 1. Objetivo del módulo

Regular el acceso seguro de los estudiantes validando identidades mediante tokens JWT cifrados, coordinando el registro inicial y el completado de datos con los catálogos de la institución.

## 2. Alcance

- **Incluye:** Registro de usuario, login por email/password, generación de JWT, validación de tokens, endpoints públicos/protegidos.
- **Incluye:** Completado de perfil con datos de catálogo institucional (carrera, facultad, semestre).
- **No incluye:** SSO, OAuth2 social, refresh token rotation (pendiente).

## 3. Lógica de negocio

- **Registro:**
  - El email institucional debe pertenecer a un dominio válido configurado.
  - Si el email ya existe, se rechaza la operación.
  - El password se almacena hasheado con BCrypt.
  - Usuario nuevo inicia con `isActive=true` y rol `STUDENT`.
- **Login:**
  - Se autentica con `AuthenticationManager`.
  - Si credenciales son válidas, se retorna JWT con claims: `sub` (email), `role`, `userId`, `name`.
- **Completado de datos:**
  - Post-registro, el estudiante debe seleccionar carrera, facultad y semestre desde catálogos precargados.
  - Si no completa en 7 días, la cuenta se marca como `pending-onboarding`.

## 4. Validaciones

### Entrada

- `RegisterRequest.email`: requerido, formato email, dominio institucional válido.
- `RegisterRequest.password`: requerido, min 8, max 72, debe contener mayúscula y número.
- `RegisterRequest.fullName`: requerido, min 3, max 120.
- `LoginRequest.email`: requerido, formato email válido.
- `LoginRequest.password`: requerido.

### Reglas de dominio

- Email único (`existsByEmail`).
- Catálogo institucional debe existir para la carrera seleccionada.
- Token JWT expira en 24h (access) y 7 días (refresh).

## 5. Contratos API

### REST

| Método | Ruta | Códigos | Descripción |
|--------|------|---------|-------------|
| `POST` | `/api/auth/register` | 201, 400, 422 | Registro de estudiante |
| `POST` | `/api/auth/login` | 200, 401, 422 | Inicio de sesión |
| `POST` | `/api/auth/refresh` | 200, 401 | Refrescar token |
| `GET` | `/api/auth/me` | 200, 401 | Perfil del usuario autenticado |
| `PUT` | `/api/auth/onboarding` | 200, 400, 422 | Completar datos de catálogo |

### GraphQL

- No aplica (módulo puramente REST por seguridad).

## 6. Persistencia

- Entidades: `User`, `StudentProfile`, `InstitutionalCatalog`.
- Repositorios: `UserRepository`, `StudentProfileRepository`, `CatalogRepository`.
- `User` → `StudentProfile` (1:1).
- `StudentProfile` → `InstitutionalCatalog` (M:1).

## 7. Seguridad

- Endpoints públicos: `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh`.
- Endpoints autenticados: `/api/auth/me`, `/api/auth/onboarding`.
- JWT firmado con HMAC-SHA256, secreto configurable via `JWT_SECRET`.
- Claims mínimos: `sub` (email), `role`, `userId`, `iat`, `exp`.

## 8. Errores y excepciones

| Escenario | Excepción | HTTP Status |
|-----------|-----------|-------------|
| Email ya registrado | `EmailAlreadyExistsException` | 400 |
| Credenciales inválidas | `BadCredentialsException` | 401 |
| Token expirado | `ExpiredJwtException` | 401 |
| Dominio email no válido | `InvalidEmailDomainException` | 400 |
| Catálogo no encontrado | `ResourceNotFoundException` | 404 |

## 9. Dependencias del módulo

- `JwtService` — generación y validación de tokens
- `PasswordEncoder` (BCrypt)
- `AuthenticationManager`
- `UserRepository`, `StudentProfileRepository`
- `CatalogService` — catálogos institucionales

## 10. Observabilidad

- Loggear intentos de login fallidos (sin exponer credenciales).
- Loggear registros exitosos con userId.
- Métrica: contador de registros vs. onboarding completado.

## 11. Casos de prueba sugeridos

- Registro exitoso retorna JWT y 201.
- Login con password incorrecto retorna 401.
- Registro duplicado por email retorna 400.
- Token expirado en endpoint protegido retorna 401.
- Onboarding con catálogo válido completa el perfil.
- Onboarding con catálogo inexistente retorna 404.

## 12. TODO / Pendientes

- Implementar refresh token rotation.
- Agregar rate limiting en login (ej: 5 intentos por minuto).
- Verificación de email mediante OTP.
- Integración con LDAP institucional (futuro).

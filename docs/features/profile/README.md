# Módulo: Configuración de Perfil

## 1. Objetivo del módulo

Gestionar la personalización de la cuenta del estudiante: datos biográficos, carrera, intereses, avatar y portada, con soporte para carga de imágenes y previsualización.

## 2. Alcance

- **Incluye:** Actualización de perfil (biografía, carrera, intereses, handle).
- **Incluye:** Carga de avatar y portada con validación de formato/tamaño.
- **Incluye:** Consulta de perfil público por handle (UUID/slug).
- **No incluye:** Eliminación de cuenta, cambio de email/password (está en Auth).

## 3. Lógica de negocio

- El handle debe ser único en todo el sistema.
- La carrera e intereses se validan contra catálogos institucionales (`InstitutionalCatalog`).
- Avatar y portada se almacenan en servicio externo (S3-compatible), en BD solo la URL.
- Al actualizar avatar, la imagen anterior se elimina del storage si ya no se referencia.
- El perfil público siempre es accesible sin autenticación (solo lectura).

## 4. Validaciones

### Entrada

- `ProfileUpdateRequest.bio`: opcional, max 500 caracteres.
- `ProfileUpdateRequest.handle`: opcional, min 3, max 30, regex `^[a-z0-9-]+$`.
- `ProfileUpdateRequest.careerId`: debe existir en `InstitutionalCatalog`.
- `ProfileUpdateRequest.interests`: array de UUIDs válidos en catálogo.

### Reglas de dominio

- Handle único (consulta `existsByHandle`).
- El estudiante solo puede editar su propio perfil (ownership).
- Las imágenes se procesan y redimensionan antes de almacenar.

## 5. Contratos API

### REST

| Método | Ruta | Códigos | Descripción |
|--------|------|---------|-------------|
| `GET` | `/api/profile/{handle}` | 200, 404 | Perfil público por handle |
| `PUT` | `/api/profile/me` | 200, 400, 422 | Actualizar perfil propio |
| `POST` | `/api/profile/me/avatar` | 200, 400, 413 | Subir avatar |
| `POST` | `/api/profile/me/cover` | 200, 400, 413 | Subir portada |
| `DELETE` | `/api/profile/me/avatar` | 204 | Eliminar avatar |
| `DELETE` | `/api/profile/me/cover` | 204 | Eliminar portada |

### GraphQL

- No aplica (CRUD de perfil se maneja por REST por simplicidad multipart).

## 6. Persistencia

- Entidades: `StudentProfile`, `InstitutionalCatalog`.
- Repositorios: `StudentProfileRepository`, `CatalogRepository`.
- `StudentProfile` se relaciona 1:1 con `User` y M:1 con `InstitutionalCatalog`.
- Intereses: tabla `student_interests` (M:M con `InstitutionalCatalog`).
- URLs de imágenes se almacenan como `VARCHAR(512)` en `StudentProfile`.

## 7. Seguridad

- `GET /api/profile/{handle}` — público.
- `PUT /api/profile/me`, `POST /api/profile/me/avatar`, `POST /api/profile/me/cover` — autenticado, ownership validado.

## 8. Errores y excepciones

| Escenario | Excepción | HTTP Status |
|-----------|-----------|-------------|
| Handle duplicado | `HandleAlreadyExistsException` | 400 |
| Perfil no encontrado | `ResourceNotFoundException` | 404 |
| Imagen excede tamaño | `FileTooLargeException` | 413 |
| Formato no soportado | `InvalidFileFormatException` | 400 |
| No autorizado (otro usuario) | `AccessDeniedException` | 403 |

## 9. Dependencias del módulo

- `StudentProfileRepository`, `CatalogRepository`
- `FileStorageService` — almacenamiento de imágenes
- `ImageProcessingService` — redimensionamiento y optimización
- `UserRepository` — validación de handle único

## 10. Observabilidad

- Loggear actualizaciones de perfil (userId, campos modificados).
- Métrica: tamaño promedio de imágenes subidas.
- Audit trail de cambios de handle.

## 11. Casos de prueba sugeridos

- Actualización exitosa de perfil retorna 200 y datos reflejados.
- Handle duplicado retorna 400.
- Imagen > 5MB retorna 413.
- Perfil público retorna datos correctos (sin email ni datos sensibles).
- Intento de editar perfil ajeno retorna 403.

## 12. TODO / Pendientes

- Implementar crop de imagen antes del upload.
- Agregar endpoint para listar intereses disponibles desde catálogo.
- Soporte para múltiples idiomas en biografía.

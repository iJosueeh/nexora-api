# Requerimientos: Configuración de Perfil

## 1. Visión General

Módulo backend que gestiona la personalización de la cuenta del estudiante: datos biográficos, carrera, intereses, avatar y portada, con soporte para carga de imágenes y consulta pública de perfiles.

---

## 2. Requerimientos Funcionales

| ID | Requerimiento | Prioridad | Estimación |
|----|--------------|-----------|------------|
| RF-PRO-01 | Exponer `GET /api/profile/{handle}` para perfil público | Alta | 5 pts |
| RF-PRO-02 | Exponer `PUT /api/profile/me` para actualizar perfil propio | Alta | 8 pts |
| RF-PRO-03 | Exponer `POST /api/profile/me/avatar` para subir avatar | Alta | 8 pts |
| RF-PRO-04 | Exponer `POST /api/profile/me/cover` para subir portada | Alta | 8 pts |
| RF-PRO-05 | Exponer `DELETE /api/profile/me/avatar` y `DELETE /api/profile/me/cover` | Media | 5 pts |
| RF-PRO-06 | Validar handle único (unique constraint + búsqueda) | Alta | 3 pts |
| RF-PRO-07 | Validar carrera e intereses contra `InstitutionalCatalog` | Alta | 5 pts |
| RF-PRO-08 | Redimensionar y optimizar imágenes al subir | Media | 8 pts |
| RF-PRO-09 | Eliminar imagen anterior de storage al reemplazar avatar/portada | Media | 5 pts |
| RF-PRO-10 | Generar URL prefirmada para descarga de imágenes (15 min exp) | Alta | 5 pts |

**Total estimado:** 60 pts

---

## 3. Requerimientos No Funcionales

| ID | Requerimiento | Tipo | Métrica |
|----|--------------|------|---------|
| RNF-PRO-01 | Upload de avatar < 3s para archivos < 1MB | Rendimiento | 3s |
| RNF-PRO-02 | Consulta de perfil público < 200ms (p95) | Rendimiento | 200ms |
| RNF-PRO-03 | Solo el dueño puede modificar su perfil | Seguridad | — |
| RNF-PRO-04 | Las URLs de imágenes expiran a los 15 min | Seguridad | 15 min |
| RNF-PRO-05 | Avatar redimensionado a 256x256px (max) | Almacenamiento | — |
| RNF-PRO-06 | Portada redimensionada a 1200x400px (max) | Almacenamiento | — |
| RNF-PRO-07 | Formatos aceptados: image/png, image/jpeg, image/webp | Compatibilidad | — |

---

## 4. Historias de Usuario

**HU-PRO-01:** Como estudiante, quiero tener un handle único para compartir mi perfil.
**HU-PRO-02:** Como estudiante, quiero actualizar mi foto de perfil para personalizar mi cuenta.
**HU-PRO-03:** Como estudiante, quiero seleccionar mi carrera desde un catálogo oficial.
**HU-PRO-04:** Como usuario, quiero ver el perfil público de otros estudiantes.

---

## 5. Reglas de Negocio

| ID | Regla | Excepción |
|----|-------|-----------|
| RN-PRO-01 | Handle único por sistema | `HandleAlreadyExistsException` |
| RN-PRO-02 | Solo el owner puede editar | `AccessDeniedException` |
| RN-PRO-03 | Avatar 2MB, Portada 5MB | `FileTooLargeException` |
| RN-PRO-04 | Solo PNG/JPG/WEBP | `InvalidFileFormatException` |
| RN-PRO-05 | Catálogo debe existir | `ResourceNotFoundException` |
| RN-PRO-06 | Al reemplazar imagen, eliminar anterior de storage | — |

---

## 6. Casos de Uso

### CU-PRO-01: Actualizar perfil
- **Endpoint:** `PUT /api/profile/me`
- **Actor:** Estudiante autenticado
- **Validaciones:** handle único, catálogo existente, bio ≤ 500 chars
- **Respuesta:** 200 + `ProfileResponse`

### CU-PRO-02: Subir avatar
- **Endpoint:** `POST /api/profile/me/avatar` (multipart)
- **Actor:** Estudiante autenticado
- **Validaciones:** formato imagen, ≤ 2MB
- **Procesamiento:** redimensionar → subir a S3 → actualizar URL
- **Respuesta:** 200 + `{ "avatarUrl": "..." }`

### CU-PRO-03: Ver perfil público
- **Endpoint:** `GET /api/profile/{handle}`
- **Actor:** Público
- **Respuesta:** 200 + datos públicos (sin email, sin datos sensibles)
- **Error:** 404 si no existe

---

## 7. Criterios de Aceptación

### CA-PRO-01: Handle único
```
Given un perfil con handle "maria.garcia"
When otro estudiante intenta usar "maria.garcia"
Then el sistema retorna 400 "Handle already exists"
```

### CA-PRO-02: Subir avatar
```
Given un estudiante autenticado
When sube un archivo PNG de 500KB como avatar
Then el sistema redimensiona a 256x256
And almacena en S3 bajo /avatars/{userId}.webp
And retorna 200 con la URL pública
```

### CA-PRO-03: Perfil público
```
Given un handle válido "maria.garcia"
When se consulta GET /api/profile/maria.garcia
Then el sistema retorna 200 con: handle, fullName, bio, avatarUrl, coverUrl, career, interests
And NO incluye email, isActive, role
```

---

## 8. Matriz de Trazabilidad

| RF | Controller | Service | Repository | Entity |
|----|-----------|---------|------------|--------|
| RF-PRO-01 | `ProfileController` | `ProfileService` | `StudentProfileRepository` | `StudentProfile` |
| RF-PRO-02 | `ProfileController` | `ProfileService` | `StudentProfileRepository` | `StudentProfile` |
| RF-PRO-03 | `ProfileController` | `FileStorageService` | `StudentProfileRepository` | `StudentProfile` |
| RF-PRO-04 | `ProfileController` | `FileStorageService` | `StudentProfileRepository` | `StudentProfile` |
| RF-PRO-06 | `ProfileController` | `ProfileService` | `StudentProfileRepository` | `StudentProfile` |
| RF-PRO-07 | `ProfileController` | `CatalogService` | `CatalogRepository` | `InstitutionalCatalog` |

---

## 9. Dependencias

| Dependencia | Tipo |
|-------------|------|
| `StudentProfileRepository` | Repositorio |
| `CatalogRepository` | Repositorio |
| `FileStorageService` | Servicio |
| `ImageProcessingService` (Thumbnailator o similar) | Librería |
| `UserRepository` | Repositorio |
| S3-compatible storage | Infraestructura |

---

## 10. Priorización

### MVP (Sprint 1-2)
RF-PRO-01, RF-PRO-02, RF-PRO-03, RF-PRO-04, RF-PRO-06, RF-PRO-07, RF-PRO-10

### Fase 2 (Sprint 3-4)
RF-PRO-05, RF-PRO-08, RF-PRO-09

---

## 11. TODOs

| ID | Tarea | Prioridad | Esfuerzo |
|----|-------|-----------|----------|
| TODO-PRO-01 | Implementar servicio de redimensionamiento de imágenes con Thumbnailator | Media | 6h |
| TODO-PRO-02 | Limpieza automática de imágenes huérfanas en storage (scheduled job) | Media | 4h |
| TODO-PRO-03 | Endpoint para listar catálogo de carreras e intereses | Alta | 3h |
| TODO-PRO-04 | Validar que el usuario no exceda cuota de almacenamiento | Baja | 3h |
| TODO-PRO-05 | Tests de integración: upload + redimension + verificación en storage | Alta | 8h |
| TODO-PRO-06 | Tests unitarios: validación de handle, formato, tamaño | Alta | 4h |
| TODO-PRO-07 | Cache de perfiles públicos en Redis | Baja | 6h |
| TODO-PRO-08 | Soporte para WebP como formato de salida predeterminado | Media | 3h |

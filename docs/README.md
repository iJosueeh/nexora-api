# Nexora Core — Documentación de Módulos (Backend)

Este directorio contiene la documentación funcional y técnica de cada módulo (feature) del backend Spring Boot.

## Objetivo

- Estandarizar cómo documentamos lógica de negocio.
- Dejar claras reglas, validaciones y contratos API.
- Facilitar onboarding del equipo backend.
- Usar esta estructura como base para nuevos módulos.

## Estructura

```text
docs/
  README.md
  features/
    _template/README.md       # Plantilla para nuevos módulos
    auth/README.md             # Autenticación y Seguridad (REST + JWT)
    user/README.md             # Usuarios base
    profile/README.md          # Configuración de Perfil
    security/README.md         # Seguridad y Filtros JWT
    graphql/README.md          # Capa GraphQL base
    feed/README.md             # Muro de Publicaciones e Interacción
    comments/README.md         # Sistema de Comentarios Anidados
    notification/README.md     # Notificaciones en Vivo (WebSocket)
    resources/README.md        # Repositorio de Recursos Académicos
    events/README.md           # Eventos y Grupos de Estudio
    bookmarks/README.md        # Marcadores y Colecciones Guardadas
    management/README.md       # Administración y Moderación
    infrastructure/README.md   # Persistencia e Infraestructura en la Nube
```

## Convenciones

- Cada feature sigue el modelo **Feature-First Packaging**:
  - `domain/entity/` — Entidades JPA
  - `domain/dto/` — DTOs
  - `repository/` — Spring Data JPA
  - `service/` — Lógica de negocio
  - `graphql/` — Resolvers GraphQL (si aplica)
  - `rest/` — Controladores REST (si aplica)
- El esquema GraphQL en `schema.graphqls` es el contrato de verdad.

## Cómo documentar un nuevo módulo

1. Crear carpeta en `docs/features/<nombre-modulo>/`.
2. Copiar la plantilla desde `docs/features/_template/README.md`.
3. Completar cada sección con comportamiento real del código.
4. Agregar referencias a clases, endpoints y propiedades.
5. Mantener el documento actualizado con cada cambio funcional.

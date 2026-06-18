# 📖 Documentación Swagger / OpenAPI - DonaTrack

Esta carpeta contiene las especificaciones **OpenAPI 3.0** para los tres microservicios del proyecto DonaTrack.

## 📁 Archivos

| Archivo | Servicio | Puerto | Descripción |
|---------|----------|--------|-------------|
| `servicio-donaciones-swagger.yaml` | servicio-donaciones | `8080` | Donantes, Donaciones, Entidades Beneficiarias, Matchmaking, Necesidades |
| `servicio-incentivos-swagger.yaml` | servicio-incentivos | `8081` | Perfiles de incentivos, Misiones, Insignias, Métricas, Ranking |
| `servicio-notificaciones-swagger.yaml` | servicio-notificaciones | `8082` | Envío y consulta de notificaciones |

## 🔍 Cómo visualizar la documentación

### Opción 1: Swagger Editor Online
1. Ir a [editor.swagger.io](https://editor.swagger.io/)
2. Copiar y pegar el contenido de cualquiera de los archivos `.yaml`
3. La interfaz interactiva se renderizará automáticamente

### Opción 2: Swagger UI con Docker
Desde la carpeta docs ejecutar en la terminal:
```bash
# Servicio de Donaciones
docker run -d -p 9090:8080 \
  --name swagger-donaciones \
  -v $(pwd)/swagger:/app \
  -e SWAGGER_JSON=/app/servicio-donaciones-swagger.yaml \
  swaggerapi/swagger-ui

# Servicio de Incentivos
docker run -d -p 9091:8080 \
  --name swagger-incentivos \
  -v $(pwd)/swagger:/app \
  -e SWAGGER_JSON=/app/servicio-incentivos-swagger.yaml \
  swaggerapi/swagger-ui

# Servicio de Notificaciones
docker run -d -p 9092:8080 \
  --name swagger-notificaciones \
  -v $(pwd)/swagger:/app \
  -e SWAGGER_JSON=/app/servicio-notificaciones-swagger.yaml \
  swaggerapi/swagger-ui
```

### Opción 3: Extensión de VS Code
Instalar la extensión **"Swagger Viewer"** o **"OpenAPI (Swagger) Editor"** y abrir cualquiera de los archivos `.yaml`.

## 📋 Resumen de Endpoints

### servicio-donaciones (`:8080`)

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/donaciones` | Listar donaciones (filtro opcional por `donanteId`) |
| `POST` | `/donaciones` | Crear nueva donación |
| `GET` | `/donaciones/{id}` | Obtener donación por ID |
| `PUT` | `/donaciones/{id}` | Actualizar donación |
| `DELETE` | `/donaciones/{id}` | Eliminar donación |
| `POST` | `/donaciones/entregar` | Registrar entrega de donación segmentada |
| `GET` | `/donantes` | Listar todos los donantes |
| `GET` | `/donantes/{email}` | Buscar donante por email |
| `POST` | `/donantes/humano` | Crear donante persona humana |
| `POST` | `/donantes/juridico` | Crear donante persona jurídica |
| `PUT` | `/donantes/{email}/humano` | Actualizar donante humano |
| `PUT` | `/donantes/{email}/juridico` | Actualizar donante jurídico |
| `DELETE` | `/donantes/{email}` | Eliminar donante |
| `POST` | `/donantes/importar` | Importar donantes desde CSV |
| `POST` | `/entidadBeneficiaria` | Crear entidad beneficiaria |
| `GET` | `/entidadBeneficiaria` | Listar entidades beneficiarias |
| `GET` | `/entidadBeneficiaria/{id}` | Buscar entidad por ID |
| `PUT` | `/entidadBeneficiaria/{id}` | Actualizar entidad beneficiaria |
| `DELETE` | `/entidadBeneficiaria/{id}` | Eliminar entidad beneficiaria |
| `GET` | `/matchmaking/ranking` | Ranking para una donación segmentada |
| `GET` | `/matchmaking/ranking/donante/{donanteId}` | Ranking para todas las donaciones de un donante |
| `POST` | `/matchmaking/asignar` | Asignar donación a entidad |
| `POST` | `/api/necesidades` | Crear necesidad |
| `GET` | `/api/necesidades/{id}` | Obtener necesidad por ID |
| `PUT` | `/api/necesidades/{id}` | Actualizar necesidad |
| `DELETE` | `/api/necesidades/{id}` | Eliminar necesidad |
| `GET` | `/api/necesidades/entidad/{entidadId}` | Listar necesidades por entidad |

### servicio-incentivos (`:8081`)

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/perfil` | Crear perfil de incentivos |
| `GET` | `/perfil/{donanteId}` | Obtener perfil completo |
| `POST` | `/perfil/entrega` | Procesar evento de entrega |
| `GET` | `/perfil/misiones/{donanteId}` | Obtener misiones del donante |
| `GET` | `/perfil/insignias/{donanteId}` | Obtener insignias del donante |
| `GET` | `/perfil/metricas/{donanteId}` | Obtener métricas de actividad |
| `GET` | `/perfil/ranking` | Ranking global de donantes |
| `GET` | `/perfil/ranking/{donanteId}` | Posición de un donante en el ranking |

### servicio-notificaciones (`:8082`)

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/api/notificaciones/notificar` | Enviar notificación por servicio externo |
| `POST` | `/api/notificaciones/` | Crear notificación interna |
| `GET` | `/api/notificaciones/` | Listar todas las notificaciones |
| `GET` | `/api/notificaciones/personas/{idPersona}` | Notificaciones por persona |

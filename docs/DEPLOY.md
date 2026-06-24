# Despliegue en la Nube - Render

## Requisitos

- Cuenta en [Render](https://render.com) (gratis, sin tarjeta)
- Repositorio GitHub público o conectado a Render

## Paso a paso

### 1. Conectar el repositorio

1. Ir a https://dashboard.render.com
2. Clic en **New** → **Blueprint**
3. Conectar el repositorio `aholman-utn/donatrack`
4. Render detecta automáticamente `render.yaml` y crea los 3 servicios

### 2. Deploy manual (alternativa sin Blueprint)

Si preferís crear los servicios uno por uno:

1. **New** → **Web Service**
2. Conectar repo → branch `main`
3. Runtime: **Docker**
4. Dockerfile Path: `./Dockerfile.donaciones` (o `.incentivos` / `.notificaciones`)
5. Plan: **Free**
6. Clic en **Deploy**

### 3. URLs resultantes

Después del deploy, cada servicio tendrá una URL pública:

| Servicio | URL (ejemplo) |
|----------|---------------|
| Donaciones | `https://donatrack-donaciones.onrender.com` |
| Incentivos | `https://donatrack-incentivos.onrender.com` |
| Notificaciones | `https://donatrack-notificaciones.onrender.com` |

### 4. Swagger público

Una vez desplegado, Swagger estará disponible en:

```
https://donatrack-donaciones.onrender.com/swagger-ui/index.html
```

### 5. Variables de entorno

Configurar en el dashboard de Render si es necesario:

| Variable | Descripción |
|----------|-------------|
| `PORT` | Puerto (Render lo asigna automáticamente) |
| `SPRING_PROFILES_ACTIVE` | `prod` para producción |
| `DB_URL` | URL de base de datos (cuando se agregue persistencia) |

## Limitaciones del plan gratuito

- Los servicios se **duermen tras 15 minutos** sin tráfico
- La primera request tras dormir tarda **~30 segundos** (cold start de JVM)
- **750 horas/mes** de uso gratuito (suficiente para los 3 servicios)
- Sin dominio personalizado en plan free

## Docker local (para probar antes de subir)

```bash
# Build
docker build -f Dockerfile.donaciones -t donatrack-donaciones .

# Run
docker run -p 8080:8080 donatrack-donaciones

# Verificar
curl http://localhost:8080/swagger-ui/index.html
```

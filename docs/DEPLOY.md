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

## Instructivo para compañeros (un servicio por persona)

Como el plan gratuito de Render solo permite **1 web service por cuenta**, cada compañero debe desplegar un servicio diferente:

| Compañero | Servicio | Dockerfile Path |
|-----------|----------|-----------------|
| Compañero 1 | Donaciones | `./Dockerfile.donaciones` |
| Compañero 2 | Incentivos | `./Dockerfile.incentivos` |
| Compañero 3 | Notificaciones | `./Dockerfile.notificaciones` |

### Paso a paso para cada compañero

1. **Crear cuenta** en https://render.com (gratis, sin tarjeta)
2. Clic en **New** → **Web Service**
3. Seleccionar **Build and deploy from a Git repository** → Next
4. Conectar GitHub y seleccionar el repo `aholman-utn/donatrack`
5. Configurar:
   - **Name**: `donatrack-donaciones` (o `incentivos` / `notificaciones`)
   - **Branch**: `main`
   - **Runtime**: `Docker`
   - **Dockerfile Path**: ver tabla de arriba
   - **Instance Type**: `Free`
6. **Environment Variables**: dejar vacío
7. Clic en **Deploy Web Service**
8. Esperar ~5 minutos a que buildee
9. Render te da una URL pública tipo `https://donatrack-donaciones.onrender.com`

### Verificar que funciona

Abrir en el navegador:
```
https://[tu-url].onrender.com/swagger-ui/index.html
```

Si tarda en cargar la primera vez (~30 seg), es normal — el servicio estaba dormido.

### Después del deploy: conectar servicios entre sí

Una vez que los 3 estén desplegados, el compañero que tiene **Donaciones** debe agregar esta variable de entorno en Render:

| Key | Value |
|-----|-------|
| `SERVICES_INCENTIVOS_URL` | `https://donatrack-incentivos.onrender.com/perfil/entrega` |

(Ir a Dashboard → servicio donaciones → Environment → Add Environment Variable)

## Docker local (para probar antes de subir)

```bash
# Build
docker build -f Dockerfile.donaciones -t donatrack-donaciones .

# Run
docker run -p 8080:8080 donatrack-donaciones

# Verificar
curl http://localhost:8080/swagger-ui/index.html
```

# TP ANUAL [link](https://suriweb.com.ar/archivos/general/DDS-TP-Anual-2026-CursoK3002-Entrega1.pdf)
GRUPO 8: 
- Emilio Alberto Maidana 209.063-6
- Matías Santana 178.361-0
- Ignacio durandez 208.966-0
- Eduardo Gaidano 213.723-9
- Lautaro Corfield 175.519-5
- Anabella Muñoz Holman 215.594-1
- German Fabrizio Gomez 203.684-8
- Lucas Pangaro 164.142-6


# Donatrack

Sistema de gestión de donaciones desarrollado con Spring Boot.

---

## ⚙️ Requisitos Previos

- **Java JDK 21** (Amazon Corretto 21 o similar)
- **Maven 3.9+** (incluido via Maven Wrapper en el proyecto)

---

## 🛠️ Configuración del entorno

### 1. Verificar que tenés un JDK 21 instalado

```bash
# Verificar si javac está disponible
javac -version
```

Si `javac` no se encuentra, necesitás configurar `JAVA_HOME` apuntando a un JDK 21. En este proyecto usamos Corretto 21:

```bash
export JAVA_HOME=~/.jdks/corretto-21.0.9
export PATH=$JAVA_HOME/bin:$PATH
```

> **Tip:** Agregá esas líneas a tu `~/.bashrc` o `~/.zshrc` para que sea permanente.

### 2. Verificar la configuración

```bash
java -version   # Debería mostrar openjdk 21.x.x
javac -version  # Debería mostrar javac 21.x.x
```

---

## � Compilar el proyecto

El proyecto incluye un Maven Wrapper (`mvnw`), así que no necesitás instalar Maven globalmente.

```bash
# Compilar sin ejecutar tests
./mvnw clean compile

# Compilar e instalar en el repositorio local (sin tests)
./mvnw clean install -DskipTests
```

---

## 🧪 Ejecutar los tests

El proyecto usa **JUnit 5** con `maven-surefire-junit5-tree-reporter` para una salida en formato de árbol legible.

### Correr todos los tests

```bash
./mvnw test
```

### Correr una clase de test específica

```bash
./mvnw test -Dtest=DonacionTest
```

### Correr un método de test específico

```bash
./mvnw test -Dtest=DonacionTest#testSegmentarDonacion
```

---
# Para ejecutar los servicios
./mvnw spring-boot:run -pl servicio-donaciones                                  
./mvnw spring-boot:run -pl servicio-incentivos                                  
./mvnw spring-boot:run -pl servicio-notificaciones                                  
./mvnw spring-boot:run -pl servicio-logistica


## Capas
### 🛂 Controller.
**Responsabilidades:**
- Recibir peticiones HTTP y devolver respuestas HTTP.
- Validar y transformar los datos de entrada (DTO → dominio).
- Delegar la lógica al Service.

**Lo que NO hace:**
- No contiene lógica de negocio.
- No accede directamente al repositorio.

Las rutas están centralizadas en DonanteRoutes para tenerlas en un solo lugar

---

## 📁 Estructura del proyecto

```
src/
├── main/
│   ├── java/com/tp/donatrack/
│   │   ├── DonatrackApplication.java        # Clase principal
│   │   ├── controllers/                     # Controladores REST
│   │   ├── domain/                          # Modelo de dominio
│   │   │   ├── bien/                        # Bienes (duraderos, perecederos)
│   │   │   ├── contacto/                    # Medios de contacto
│   │   │   ├── donacion/                    # Donaciones y segmentación
│   │   │   ├── donante/                     # Donantes
│   │   │   ├── entidad/                     # Entidades beneficiarias
│   │   │   ├── necesidad/                   # Necesidades
│   │   │   ├── notificacion/                # Notificaciones
│   │   │   ├── persona/                     # Personas (humana, jurídica)
│   │   │   └── ubicacion/                   # Ubicaciones
│   │   ├── repositories/                    # Repositorios
│   │   └── services/                        # Servicios de negocio
│   └── resources/
│       └── application.yaml                 # Configuración de la aplicación
└── test/
    └── java/com/tp/donatrack/               # Tests unitarios
```

---

## 🎨 Bocetos de Interfaz de Usuario

Los bocetos de las pantallas del sistema se encuentran en la carpeta `bocetos/`. Son archivos HTML estáticos que se abren directamente en el navegador sin necesidad de levantar ningún servidor.

### Cómo acceder

```bash
# Abrir la landing page (índice de todos los bocetos)
xdg-open bocetos/index.html
```

O simplemente hacé doble clic en `bocetos/index.html` desde el explorador de archivos.

### Pantallas disponibles

| Rol     | Pantalla                | Archivo                            |
|---------|-------------------------|------------------------------------|
| Público | Landing Page            | `bocetos/index.html`               |
| Público | Inicio de Sesión        | `bocetos/login.html`               |
| Público | Registro                | `bocetos/registro.html`            |
| Donante | Dashboard               | `bocetos/donante-dashboard.html`   |
| Donante | Mis Donaciones          | `bocetos/donante-donaciones.html`  |
| Donante | Entidades Beneficiarias | `bocetos/donante-entidades.html`   |
| Entidad | Dashboard               | `bocetos/entidad-dashboard.html`   |
| Entidad | Gestión de Necesidades  | `bocetos/entidad-necesidades.html` |
| Entidad | Donaciones Asignadas    | `bocetos/entidad-donaciones.html`  |
| Admin   | Dashboard               | `bocetos/admin-dashboard.html`     |
| Admin   | Gestión de Donantes     | `bocetos/admin-donantes.html`      |
| Admin   | Registro de Donaciones  | `bocetos/admin-donaciones.html`    |
| Admin   | Importación CSV         | `bocetos/admin-importar.html`      |

---

## 🔧 Tecnologías

- **Java 21**
- **Spring Boot 3.4.5**
- **Lombok** (generación de getters/setters/constructores)
- **JUnit 5** (testing)
- **Maven** (gestión de dependencias y build)
- **Springdoc OpenAPI** (documentación de API REST con Swagger UI)

## API disponible

### Importar donantes por CSV

```http
POST /donantes/importar
Content-Type: multipart/form-data
```

Parametro:

| Campo | Tipo | Descripcion |
| --- | --- | --- |
| `file` | CSV | Archivo con donantes a importar |

Ejemplo:

```powershell
curl.exe -X POST http://localhost:8080/donantes/importar `
  -F "file=@C:\ruta\a\donantes.csv"
```

### Formato esperado del CSV

El importador saltea la primera linea como cabecera y procesa las filas siguientes:

```csv
tipoPersona,tipoDoc,documento,nombre,email,telefono
HUMANA,DNI,12345678,Juan Perez,juan@example.com,111
JURIDICA,CUIT,30-12345678-9,ONG Donar,contacto@ong.org,222
```

Columnas:

| Columna | Uso actual |
| --- | --- |
| `tipoPersona` | `HUMANA` crea una `PersonaHumana`; otro valor crea una `PersonaJuridica`. |
| `tipoDoc` | Se lee desde el CSV, pero actualmente no se persiste. |
| `documento` | Para persona humana se limpian caracteres no numericos y se guarda como numero de documento. |
| `nombre` | Nombre completo o razon social. |
| `email` | Medio de contacto y clave usada para buscar donantes existentes. |
| `telefono` | Medio de contacto telefonico. |

Al crear un donante nuevo desde CSV, el servicio genera una password temporal y envia una notificacion de bienvenida usando el `NotificacionService`.

Nota: en esta branch el servicio ya busca si existe un donante por email; la rama de actualizacion del donante existente esta indicada en el codigo como pendiente.

## Modelo de dominio

### Donantes y personas

- `domain/donante/Donante`: entidad que vincula el registro de donante con una `Persona`.
- `domain/persona/Persona`: clase base abstracta con notificaciones, direccion y medios de contacto.
- `PersonaHumana`: representa personas fisicas.
- `PersonaJuridica`: representa organizaciones con tipo, razon social y rubro.
- `TipoPersona`: enum para distinguir tipos de persona.
- `TipoOrganizacion`: enum para clasificar organizaciones como gubernamental, ONG, empresa o institucion.

### Bienes

- `Bien`: clase abstracta con nombre, descripcion, foto y subcategoria.
- `BienDuradero`: segmenta por `EstadoBien`.
- `BienPerecedero`: segmenta por fecha de vencimiento.
- `ClaveAgrupacion`: record que agrupa por subcategoria y criterio de segmentacion.
- `Categoria`, `SubCategoria` y `Unidad`: clasifican los bienes.

### Donaciones

- `Donacion`: recibe una lista de bienes y genera automaticamente sus `DonacionSegmentada`.
- `DonacionSegmentada`: agrupa bienes compatibles y puede adjudicarse a una entidad beneficiaria.
- `EstadoDonacion`: indica si la donacion general esta pendiente o adjudicada.
- `EstadoDonacionSegmentada`: indica el estado de cada segmento.

La segmentacion actual agrupa bienes por:

```text
subcategoria + criterio propio del bien
```

Ejemplos:

- Bien duradero: subcategoria + estado.
- Bien perecedero: subcategoria + fecha de vencimiento.

### Entidades beneficiarias y necesidades

- `EntidadBeneficiaria`: recibe donaciones segmentadas y administra necesidades.
- `NecesidadMaterial`: base para necesidades.
- `NecesidadRecurrente`: necesidad con periodo.
- `NecesidadExtraordinaria`: necesidad con causa puntual.
- `EstadoNecesidad`: estado de satisfaccion de la necesidad.

### Notificaciones

- `Notificacion`: mensaje con asunto, cuerpo, fecha y tipo.
- `TipoNotificacion`: clasifica la notificacion.
- `iNotificador`: interfaz strategy para enviar notificaciones.
- `NotificadorEmail`, `NotificadorTelefono`, `NotificadorWhatsApp`: implementaciones concretas.
- `TipoNotificador`: enum para seleccionar el canal.
- `NotificacionService`: resuelve el notificador por tipo y envia la notificacion.

## Estructura del proyecto

```text
src/main/java/com/tp/donatrack
|-- DonatrackApplication.java
|-- ImportacionResponseDTO.java
|-- controllers
|   `-- DonanteController.java
|-- repositories
|   `-- DonanteRepository.java
|-- services
|   |-- DonanteService.java
|   |-- DonacionService.java
|   |-- NotificacionService.java
|   `-- PersonaService.java
`-- domain
    |-- bien
    |-- donacion
    |-- donante
    |-- entidad
    |-- necesidad
    |-- notificacion
    |-- notificador
    |-- persona
    `-- ubicacion
```

## Configuracion

Archivo principal:

```text
src/main/resources/application.yaml
```

Configuracion actual:

- Nombre de aplicacion: `donatrack`
- Maximo tamano de archivo multipart: `10MB`
- Maximo tamano de request multipart: `10MB`

---

## 📡 API REST - Swagger UI

El proyecto expone su documentación de API automáticamente mediante Swagger UI.

### Levantar el servicio

```bash
export JAVA_HOME=~/.jdks/corretto-21.0.9
export PATH=$JAVA_HOME/bin:$PATH
./mvnw spring-boot:run -pl servicio-donaciones
```

### Acceder a Swagger

Una vez levantado el servicio, abrí en el navegador:

| Recurso | URL |
|---------|-----|
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

### Puertos por servicio

| Servicio | Puerto |
|----------|--------|
| servicio-donaciones | 8080 |
| servicio-incentivos | 8081 |
| servicio-notificaciones | 8082 |
| servicio-logistica | 8083 |

---

## 📧 Configuración de Email (Resend)

El servicio de notificaciones envía emails reales usando [Resend](https://resend.com).

### Configurar para desarrollo local

1. Crear una cuenta gratuita en https://resend.com (100 emails/día gratis)
2. Obtener tu API key desde el dashboard de Resend
3. Configurar la variable de entorno antes de levantar el servicio:

```bash
export RESEND_API_KEY=tu_api_key_aca
```

4. Si no configurás la variable, el servicio usa un provider simulado que imprime en consola.

### Nota
La API key del equipo está en el archivo `SECRETS.md` (no se sube al repo). Pedila al grupo.

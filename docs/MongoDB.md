# MongoDB

El servicio `servicio-logistica` persiste en MongoDB sus envíos, rutas, camiones, choferes y eventos. Puede conectarse a una instancia local con Docker o a MongoDB Atlas. MongoDB Compass permite explorar ambas.

## Variables de entorno

`servicio-logistica/src/main/resources/application.properties` construye la URI con estas variables:

| Variable | Uso | Valor predeterminado |
| --- | --- | --- |
| `MONGODB_PROTOCOL` | Esquema de la URI (`mongodb` para una instancia normal o `mongodb+srv` para Atlas) | `mongodb` |
| `DB_HOST` | Host o dirección de MongoDB | `localhost` |
| `DB_NAME` | Base de datos del servicio | `logistica-db` |
| `MONGODB_USERNAME` | Usuario de autenticación | Obligatorio |
| `MONGODB_PASSWORD` | Contraseña de autenticación | Obligatorio |
| `DB_AUTH_DB` | Base donde se autentica el usuario | `admin` |

Como mínimo, hay que definir `MONGODB_USERNAME` y `MONGODB_PASSWORD`. Para Atlas también se debe configurar `DB_HOST` con el host del clúster. No se debe confundir `DB_NAME` (base usada por la aplicación) con `DB_AUTH_DB` (base contra la que se autentica el usuario).

En PowerShell, las variables se pueden definir para la terminal actual antes de iniciar el servicio:

```powershell
$env:MONGODB_USERNAME = "root"
$env:MONGODB_PASSWORD = "donatrack"
$env:DB_HOST = "localhost"
$env:MONGODB_PROTOCOL = "mongodb"
$env:DB_NAME = "logistica-db"
$env:DB_AUTH_DB = "admin"
./mvnw spring-boot:run -pl servicio-logistica
```

En otros sistemas, se deben exportar las mismas variables con el mecanismo de la terminal correspondiente. Las variables definidas así duran mientras siga abierta esa terminal. No se deben guardar contraseñas reales en el repositorio.

## Opción local: Docker

### Usar el Docker Compose del proyecto

Desde la raíz del repositorio, iniciar únicamente MongoDB:

```bash
docker compose -f herramientas/docker-compose.yml up -d mongo
```

El servicio queda disponible en `localhost:27017`. El Compose configura el usuario administrador `root` y la contraseña `donatrack`; esos valores deben coincidir con `MONGODB_USERNAME` y `MONGODB_PASSWORD` al iniciar Logística. El servicio se autentica contra `admin` y usa por defecto la base `logistica-db`.

Para detener el contenedor sin borrar los datos persistidos:

```bash
docker compose -f herramientas/docker-compose.yml stop mongo
```

El Compose guarda los archivos de MongoDB en el volumen `mongodb_data`. Si se levanta todo el entorno de desarrollo mediante `start-dev.ps1`, MongoDB también se inicia junto con los demás servicios definidos allí.

### Crear un contenedor propio

También se puede iniciar una instancia independiente con Docker:

```bash
docker run -d --name donatrack-mongo `
  -p 27017:27017 `
  -e MONGO_INITDB_ROOT_USERNAME=root `
  -e MONGO_INITDB_ROOT_PASSWORD=donatrack `
  -v donatrack-mongodb-data:/data/db `
  mongo:latest
```

En este ejemplo se usan las mismas credenciales que en el Compose. El volumen mantiene los datos aunque se detenga o elimine el contenedor. Si se eligen otras credenciales o un puerto, hay que ajustar las variables de entorno correspondientes para Logística y los datos de conexión en Compass.

## Opción en la nube: MongoDB Atlas

Atlas permite crear un clúster administrado en la nube. Su nivel gratuito tiene espacio de almacenamiento limitado; conviene usarlo para desarrollo y pruebas pequeñas, controlar el consumo y no asumir que alcanza para guardar indefinidamente todos los datos del equipo.

Pasos generales:

1. Crear un proyecto y un clúster en [MongoDB Atlas](https://www.mongodb.com/atlas).
2. En **Database Access**, crear un usuario de base de datos y guardar su contraseña. Este usuario es distinto de la cuenta con la que se inicia sesión en Atlas.
3. En **Network Access**, permitir la dirección IP desde la que se conectará el servicio. Evitar abrir el acceso a cualquier IP salvo que sea una necesidad temporal y controlada.
4. Obtener el host del clúster desde la opción **Connect**. Se verá parecido a `cluster0.xxxxx.mongodb.net`.
5. Configurar las variables de entorno del servicio. Por ejemplo, en PowerShell:

```powershell
$env:MONGODB_PROTOCOL = "mongodb+srv"
$env:DB_HOST = "cluster0.xxxxx.mongodb.net"
$env:DB_NAME = "logistica-db"
$env:DB_AUTH_DB = "admin"
$env:MONGODB_USERNAME = "usuario_creado_en_atlas"
$env:MONGODB_PASSWORD = "contraseña_del_usuario"
./mvnw spring-boot:run -pl servicio-logistica
```

## Ver los datos con MongoDB Compass

[MongoDB Compass](https://www.mongodb.com/products/tools/compass) es la herramienta recomendada para visualizar y consultar los datos de manera gráfica. Crear una conexión nueva con estos datos para Docker local:

- **Hostname:** `localhost`
- **Port:** `27017`
- **Authentication:** `Username / Password`
- **Username:** `root`
- **Password:** `donatrack`
- **Authentication Database:** `admin`

Luego conectarse y abrir la base `logistica-db`. MongoDB crea la base y sus colecciones cuando la aplicación escribe datos por primera vez; puede que no aparezcan inmediatamente si todavía no se registró nada.

Para Atlas, en Compass se puede pegar la URI de conexión que ofrece Atlas y completar el usuario y contraseña de **Database Access** cuando se solicite. También se puede cargar el hostname y las credenciales manualmente. La IP de la máquina debe estar permitida en **Network Access**.

## Consideración sobre el perfil local

La configuración actual activa el perfil `local`, y `MongoResetConfig` elimina la base de datos seleccionada cada vez que arranca el servicio. Por eso, con la configuración predeterminada, los datos locales se reinician al reiniciar Logística. Tener en cuenta este comportamiento antes de usar una base con datos que se quieran conservar; el volumen de Docker no evita que la propia aplicación elimine la base al arrancar.

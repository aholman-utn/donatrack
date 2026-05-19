# TP ANUAL [link](https://suriweb.com.ar/archivos/general/DDS-TP-Anual-2026-CursoK3002-Entrega1.pdf)
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

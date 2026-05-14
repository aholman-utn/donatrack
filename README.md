# TP ANUAL [link](https://suriweb.com.ar/archivos/general/DDS-TP-Anual-2026-CursoK3002-Entrega1.pdf)
- Emilio Alberto Maidana 209.063-6
- Matías Santana 178.361-0
- Ignacio durandez 208.966-0
- Eduardo Gaidano 213.723-9
- Lautaro Corfield 175.519-5
- Anabella Muñoz Holmsn 215.594-1
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
│   │   │   ├── entidad/                     # Entidades beneficiarias, donantes
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

## 🔧 Tecnologías

- **Java 21**
- **Spring Boot 3.4.5**
- **Lombok** (generación de getters/setters/constructores)
- **JUnit 5** (testing)
- **Maven** (gestión de dependencias y build)

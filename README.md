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

Este documento proporciona las instrucciones necesarias para compilar el proyecto, configurar el entorno local y ejecutar la suite de pruebas.

## ⚙️ Requisitos Previos

Asegurate de tener instalados los siguientes componentes en tu entorno de desarrollo:
* **Java** (JDK 17 o superior recomendado)
* **Maven** (3.8.x o superior)

---

## 🛠️ Primeros Pasos

Para inicializar el proyecto, descargar las dependencias necesarias y compilar el código fuente por primera vez (omitiendo la ejecución de pruebas), ejecuta el siguiente comando:

```bash
mvn clean install -DskipTests
```

# 🧪 Testing

El proyecto utiliza JUnit 5 junto con maven-surefire-junit5-tree-reporter. Esto reemplaza la salida estándar de Maven por un formato de árbol mucho más legible en la consola, utilizando los nombres descriptivos definidos en @DisplayName.

Para correr todas las pruebas unitarias y de integración del proyecto:
```bash
mvn clean test
```

Ejecutar una clase específica
```bash
mvn test -Dtest=DonacionTest
```

Ejecutar un método específico
```bash
mvn test -Dtest=DonacionTest#testSegmentarDonacion
```
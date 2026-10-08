FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copiamos los archivos de Maven y el pom padre
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Copiamos SOLO las carpetas necesarias para este microservicio
COPY commons ./commons
COPY servicio-logistica ./servicio-logistica

# Le damos permisos de ejecución al wrapper de Maven
RUN chmod +x mvnw

# Compilamos logística (el -am se encarga de compilar commons automáticamente)
RUN ./mvnw clean package -pl servicio-logistica -am -DskipTests

# Etapa 2: Imagen ligera para correr la app
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Al terminar el destino con "/", Docker sabe que es un directorio y acepta el comodín *.jar
COPY --from=build /app/servicio-logistica/target/*.jar /app/

EXPOSE 8083

# Usamos sh -c para que expanda automáticamente el *.jar al nombre real que le haya puesto Maven
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -jar /app/*.jar"]
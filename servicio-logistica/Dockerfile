FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

COPY commons ./commons
COPY servicio-logistica ./servicio-logistica
COPY servicio-donaciones ./servicio-donaciones
COPY servicio-incentivos ./servicio-incentivos
COPY servicio-notificaciones ./servicio-notificaciones
COPY api-gateway ./api-gateway

RUN chmod +x mvnw

# Compilamos logística (el -am se encarga de compilar commons automáticamente si lo necesita)
RUN ./mvnw clean package -pl servicio-logistica -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/servicio-logistica/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
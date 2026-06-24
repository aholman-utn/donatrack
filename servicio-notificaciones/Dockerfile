FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY ../pom.xml ./pom.xml
COPY ../commons ./commons
COPY ../mvnw ./mvnw
COPY ../.mvn ./.mvn
COPY servicio-notificaciones ./servicio-notificaciones

RUN chmod +x mvnw
RUN ./mvnw clean package -pl commons -am -DskipTests
RUN ./mvnw clean package -pl servicio-notificaciones -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/servicio-notificaciones/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]

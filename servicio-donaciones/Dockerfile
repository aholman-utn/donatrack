FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY ../pom.xml ./pom.xml
COPY ../commons ./commons
COPY ../mvnw ./mvnw
COPY ../.mvn ./.mvn
COPY servicio-donaciones ./servicio-donaciones

RUN chmod +x mvnw
RUN ./mvnw clean package -pl commons -am -DskipTests
RUN ./mvnw clean package -pl servicio-donaciones -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/servicio-donaciones/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

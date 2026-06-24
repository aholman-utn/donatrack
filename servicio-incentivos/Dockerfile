FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY ../pom.xml ./pom.xml
COPY ../commons ./commons
COPY ../mvnw ./mvnw
COPY ../.mvn ./.mvn
COPY servicio-incentivos ./servicio-incentivos

RUN chmod +x mvnw
RUN ./mvnw clean package -pl commons -am -DskipTests
RUN ./mvnw clean package -pl servicio-incentivos -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/servicio-incentivos/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

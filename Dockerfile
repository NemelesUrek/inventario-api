# ---- build ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp -DskipTests package

# ---- run ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/inventario-api-0.0.1-SNAPSHOT.jar app.jar
# Render/Railway inyectan el puerto en $PORT (la app lo lee en application.properties).
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

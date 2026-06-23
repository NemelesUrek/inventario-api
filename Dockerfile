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
# Flags de arranque para contenedor pequeño (Render free, 512MB / 1 vCPU compartida):
#  - SerialGC + TieredStopAtLevel=1: menos hilos de GC/JIT compitiendo -> boot más rápido.
#  - MaxRAMPercentage=65: heap acotado dejando ~180MB para metaspace/code-cache/threads.
#  - ExitOnOutOfMemoryError: si algún día hay OOM real, sale limpio para que Render reinicie.
# (Sin topes duros de metaspace/code-cache: arriesgan OOM al cargar clases sin acelerar el boot.)
# Nota: si dejas JAVA_TOOL_OPTIONS en el dashboard de Render, bórralo (estas flags son la única fuente).
ENTRYPOINT ["java", "-XX:+UseSerialGC", "-XX:TieredStopAtLevel=1", "-XX:MaxRAMPercentage=65.0", "-Xss512k", "-XX:+ExitOnOutOfMemoryError", "-jar", "app.jar"]

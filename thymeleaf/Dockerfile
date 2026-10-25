# ---------- Etapa 1: compilar ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# maven.test.skip: ni compila ni corre las pruebas (más rápido y no depende de ellas)
RUN mvn -B -q -Dmaven.test.skip=true package

# ---------- Etapa 2: ejecutar ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Valores por defecto para que Render funcione sin configurarlos a mano.
# Se pueden sobrescribir con variables de entorno (Render, docker compose).
ENV DB_PORT=5432 \
    SPRING_JPA_HIBERNATE_DDL_AUTO=update \
    APP_MAIL_ENABLED=false \
    JAVA_OPTS="-Xmx384m"

EXPOSE 8080
# Render inyecta PORT; en compose o local se usa 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]

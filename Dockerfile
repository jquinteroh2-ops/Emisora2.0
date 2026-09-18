# =====================================================================
# Dockerfile Multi-Stage para Emisora 2.0 (Spring Boot MVC + Thymeleaf)
# Etapa 1: Compilación con Maven y OpenJDK 21
# Etapa 2: Imagen final ligera con JRE 21
# =====================================================================

# Etapa 1: Construcción
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar el proyecto y empaquetar el JAR ejecutable (las pruebas se corren antes con "mvn test")
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# Etapa 2: Ejecución
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Crear usuario sin privilegios para ejecución segura
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Copiar el artefacto JAR ejecutable desde la etapa de compilación
COPY --from=build /app/target/emisora-2.0.0.jar app.jar

# Asignar permisos al usuario no privilegiado
RUN chown -R appuser:appgroup /app
USER appuser

# Puerto por defecto (Render y Railway asignan PORT dinámicamente)
ENV PORT=8080
EXPOSE 8080

# Parámetros optimizados de JVM para entornos de contenedores en la nube
# Hora de Colombia para las fechas de registro de usuarios y el reporte por fechas
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Duser.timezone=America/Bogota -Dserver.port=${PORT:-8080} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]

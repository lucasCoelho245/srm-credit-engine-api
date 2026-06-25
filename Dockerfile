# ── Stage 1: Build ──────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copia pom.xml primeiro para cache de dependências (layers Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copia sources e compila sem testes (testes rodam em CI)
COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: Runtime ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Usuário não-root (boas práticas de segurança)
RUN apk add --no-cache curl \
    && addgroup -S srm \
    && adduser -S srm -G srm
USER srm

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]

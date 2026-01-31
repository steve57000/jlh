# -------- BUILD STAGE --------
FROM maven:3.9.2-eclipse-temurin-17 AS builder
WORKDIR /app

# Pré-téléchargement des dépendances
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compile de l’application
COPY src ./src
RUN mvn clean package -DskipTests -B

# -------- RUNTIME STAGE --------
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

COPY uploads/ /app/uploads-seed/
COPY deploy/docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

# 1) Installe curl (healthcheck) + 2) normalise CRLF -> LF + 3) chmod
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl ca-certificates \
 && rm -rf /var/lib/apt/lists/* \
 && sed -i 's/\r$//' /usr/local/bin/docker-entrypoint.sh \
 && chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s \
  CMD curl --fail http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]

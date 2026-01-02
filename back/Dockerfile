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

# Copier le JAR depuis le builder
COPY --from=builder /app/target/*.jar app.jar

# Exposer le port de l’application
EXPOSE 8080

# Profil Spring par défaut (override via .env ou docker-compose)
ENV SPRING_PROFILES_ACTIVE=prod

# Healthcheck HTTP (requiert actuator sur /actuator/health)
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s \
  CMD curl --fail http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java","-jar","/app/app.jar"]

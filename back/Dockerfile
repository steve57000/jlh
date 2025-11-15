# -------- BUILD STAGE --------
FROM maven:3.9.2-eclipse-temurin-17 AS builder
WORKDIR /app

# Copier le POM pour télécharger les dépendances sans recompiler à chaque changement
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le code source et compiler
COPY src ./src
RUN mvn clean package -DskipTests -B

# -------- RUNTIME STAGE --------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copier le JAR produit par la phase de build
COPY --from=builder /app/target/*.jar app.jar

# Profil Spring par défaut (override via .env ou docker-compose)
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java","-jar","/app/app.jar"]
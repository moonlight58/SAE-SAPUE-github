# Multi-stage build pour optimiser la taille de l'image finale

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copier les fichiers de configuration Maven
COPY pom.xml .

# Télécharger les dépendances
RUN mvn dependency:go-offline -B

# Copier le code source
COPY src ./src

# Compiler et packager l'application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copier le JAR depuis le stage de build
COPY --from=build /app/target/*.jar app.jar

# Copier les fichiers de configuration
COPY config.yml /app/

# Changer le propriétaire des fichiers
RUN chown -R appuser:appgroup /app


# Basculer vers l'utilisateur non-root
USER appuser

# Port exposé (à adapter selon votre application)
EXPOSE 50010

# Commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar"]
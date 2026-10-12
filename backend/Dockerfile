# Multi-stage Dockerfile for Spring Boot 3.3 / Java 21 Backend
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
COPY src ./src
COPY ml/models/model.json ./src/main/resources/model.json

# Build production jar
RUN mvn clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Add unprivileged user for security
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

COPY --from=build /app/target/fake-news-backend-1.0.0.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

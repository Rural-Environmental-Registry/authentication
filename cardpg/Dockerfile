# Stage 1: Build the Spring Boot application using Maven
FROM maven:3.9.8-eclipse-temurin-21 AS build

ENV http_proxy=$HTTP_PROXY \
  https_proxy=$HTTPS_PROXY \
  no_proxy=$NO_PROXY

WORKDIR /app

# Copy the Maven project files (pom.xml and source code)
COPY pom.xml .
COPY src ./src

# Build the Spring Boot application
RUN mvn clean package -DskipTests

# Stage 2: Create the final Docker image
FROM eclipse-temurin:21-jre-jammy AS final

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port your Spring Boot application listens on
EXPOSE 8081

# Define the entry point to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
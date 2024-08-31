# Use a Maven image with JDK 17
FROM maven:3.8.6-eclipse-temurin-17 AS build

# Set the working directory
WORKDIR /app

# Copy the project files to the Docker image
COPY . .

# Run Maven to build the project, skipping tests
RUN mvn clean package -DskipTests

# Use a lighter base image for the runtime environment
FROM openjdk:17-jdk-slim

# Set the working directory in the runtime image
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/fruitsappbackend-0.0.1-SNAPSHOT.jar demo.jar

# Run the application
CMD ["java", "-jar", "demo.jar"]

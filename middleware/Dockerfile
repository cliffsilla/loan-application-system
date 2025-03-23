FROM eclipse-temurin:17-jdk-jammy as build

# Set the working directory
WORKDIR /app

# Copy the Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy the source code
COPY src/ src/

# Build the application
RUN ./mvnw package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

# Add Maintainer Info
LABEL maintainer="example@example.com"

# Set the working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/middleware-0.0.1-SNAPSHOT.jar app.jar

# Create directory for H2 database files
RUN mkdir -p /app/data

# Expose the port the app runs on
EXPOSE 8080

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar", "--server.address=0.0.0.0"]

# 08_Deployment.md

## Deployment

This document outlines the deployment strategy for the Loan Management Module (LMS) and the Middleware.  We will focus on deploying to Fly.io, using an H2 in-memory database for simplicity. **Note:** This setup is for demonstration purposes only. Data will NOT persist when the application restarts.

### 1. Deployment Options

*   **Local Deployment:**  Deploying the applications directly to a local machine for development and testing.
*   **Virtual Machine (VM) Deployment:** Deploying the applications to virtual machines in a cloud environment (e.g., AWS EC2, Azure VMs, Google Compute Engine).
*   **Containerized Deployment (Recommended):** Deploying the applications as Docker containers to a container orchestration platform like Kubernetes or Docker Swarm. This provides scalability, portability, and isolation.
*   **Platform-as-a-Service (PaaS) Deployment:** Deploying the applications to a PaaS platform like Heroku or AWS Elastic Beanstalk. This simplifies the deployment process and provides automatic scaling and management.
*   **Fly.io (Recommended):** A modern platform for deploying and scaling applications globally. Fly.io uses lightweight VMs (Firecracker) and makes deployment easy with its CLI.

### 2. Recommended Approach: Deployment to Fly.io

Fly.io is a great option for deploying the LMS and Middleware. It's relatively simple to use, provides global distribution, and has a free tier. **This guide assumes you are using an H2 in-memory database for simplicity. Remember data will NOT persist on restarts!**

#### 2.1. Prerequisites

1.  **Fly.io Account:** Create an account at [https://fly.io/](https://fly.io/).
2.  **Flyctl CLI:** Install the Flyctl command-line tool.  Follow the instructions on the Fly.io website for your operating system.
3.  **Docker:** Ensure Docker is installed and running locally. Fly.io uses Docker images for deployment.

#### 2.2. Dockerizing the Applications

1.  **Create a `Dockerfile` for each application (LMS and Middleware):**

    *   **Example `Dockerfile` (LMS):**

```dockerfile
FROM eclipse-temurin:17-jre-jammy

# Add Maintainer Info
LABEL maintainer="youremail@example.com"

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY target/lms-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

```
*   **Example `Dockerfile` (Middleware):**

FROM eclipse-temurin:17-jre-jammy

# Add Maintainer Info
LABEL maintainer="youremail@example.com"

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY target/middleware-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the app runs on
EXPOSE 8081

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

Build the JAR files using Maven:

mvn clean install  # In both the LMS and Middleware project directories

Build the docker images

docker build -t lms-image .  # From the LMS project directory
docker build -t middleware-image . # From the Middleware project directory

(Optional) Push the Docker images to a container registry (e.g., Docker Hub, AWS ECR, Azure Container Registry, Google Container Registry):
This step is optional. Fly.io can build the image directly from your Dockerfile. If you skip this, remove the image: lines from the fly.toml file below.

docker tag lms-image your-dockerhub-username/lms-image:latest
docker push your-dockerhub-username/lms-image:latest

docker tag middleware-image your-dockerhub-username/middleware-image:latest
docker push your-dockerhub-username/middleware-image:latest

2.3. Fly.io Deployment
Create a fly.toml file for each application (LMS and Middleware): This file configures your Fly.io application.

Example fly.toml (LMS):


app = "lms-app"  # Replace with your desired app name (must be unique on Fly.io)
primary_region = "iad"  # Choose a region close to your users

[build]
  dockerfile = "Dockerfile"

[http_service]
  internal_port = 8080
  force_https = true
  auto_stop_machines = true
  auto_start_machines = true
  min_machines_running = 1

[[vm]]
  cpu_kind = "shared"
  cpus = 1
  memory_mb = 512

[env]
  # Spring profile to use (optional, if you have a specific profile for H2)
  SPRING_PROFILES_ACTIVE = "h2"
  # Since we're using H2 in-memory, we don't need database credentials

  *   **Example `fly.toml` (Middleware):**

app = "middleware-app"  # Replace with your desired app name (must be unique on Fly.io)
primary_region = "iad"  # Choose a region close to your users

[build]
  dockerfile = "Dockerfile"

[http_service]
  internal_port = 8081
  force_https = true
  auto_stop_machines = true
  auto_start_machines = true
  min_machines_running = 1

[[vm]]
  cpu_kind = "shared"
  cpus = 1
  memory_mb = 512

Configure H2 Profile (LMS):

In your application.properties or application.yml file for the LMS project, create a profile for H2:

# application-h2.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update

Then activate that profile on Fly.io using environment variables.

Deploy the applications using the Flyctl CLI:

flyctl launch --no-deploy # Run flyctl launch to create the fly.toml, but skip deployment
# Edit the fly.toml file to add your env variables

flyctl deploy # In both the LMS and Middleware project directories

*   The `flyctl deploy` command will:
    *   Build the Docker image (if you didn't push it to a registry).
    *   Deploy the application to the Fly.io platform.

  Make sure your application is active with the h2 profile.

3. Database Configuration (H2 - IN-MEMORY - NOT PERSISTENT!)
IMPORTANT: With H2 in-memory, you do not need to create a database on Fly.io. The database will be created automatically when the application starts, but it will be empty each time the application restarts.

 You can create a profile for H2:

 4. Post-Deployment Steps
Check Application Logs:

Use flyctl logs -a <app-name> to view the application logs and check for any errors.

Scale the Applications:

Use flyctl scale count <number> to scale the number of instances of your applications as needed.

Monitor the Applications:

Use Fly.io's monitoring tools to track the performance and health of your applications.

5. Notes
Replace placeholders like lms-app and middleware-app with your actual app names.

Choose a Fly.io region that is close to your users for low latency.

REMEMBER: Data will be lost when the application restarts! This is only suitable for demonstration purposes!

For persistent data, you must use a real database (like Fly.io's PostgreSQL add-on or an external database) and configure the appropriate environment variables.


**Key Changes:**

*   **H2 Focus:** The document now emphasizes the use of H2 in-memory for simplicity.
*   **Persistence Warning:**  A prominent warning is added about the lack of data persistence with H2 in-memory.
*   **Simplified `fly.toml`:**  The `fly.toml` files are simplified, removing database credentials and adding a `SPRING_PROFILES_ACTIVE` environment variable to activate the H2 profile.
*    Added configuration for the H2 profile.
*   **Database Configuration Section:**  The database configuration section is updated to reflect the use of H2 in-memory and the fact that no database creation is needed on Fly.io.

**Your Tasks:**

1.  **Follow the instructions to deploy to Fly.io, but be aware of the limitations of using H2 in-memory.**  Only use this for testing the deployment process itself.
2.  **For a real application, you *must* switch to a persistent database like PostgreSQL.**
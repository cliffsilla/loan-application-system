# Initializing the Spring Boot Application

This document outlines the steps to initialize a new Spring Boot project for the Loan Management Module (LMS) and the Middleware.

## 1. Using Spring Initializr

* Navigate to [https://start.spring.io/](https://start.spring.io/)
* **Project:** Maven Project
* **Language:** Java
* **Spring Boot:** (Latest Stable - e.g., 3.2.x)
* **Group:** com.example (or your preferred group ID)
* **Artifact:** lms (or middleware)
* **Name:** LMS (or Middleware)
* **Description:** Loan Management System (or Middleware for Transactional Data)
* **Package name:** com.example.lms (or com.example.middleware)
* **Packaging:** Jar
* **Java:** 17 (or your preferred version)

## 2. Dependencies

Add the following dependencies:

* **Spring Web:** For building RESTful APIs.
* **Spring Data JPA:** For database access using JPA/Hibernate (LMS only).
* **PostgreSQL Driver:** For connecting to a PostgreSQL database (or your database of choice) - (LMS only).
* **Spring Integration:** For SOAP communication with the CBS.
* **Spring Cloud OpenFeign:** For declarative REST client (for communicating with the Scoring Engine) - (LMS only).
* **Lombok:** (Optional) For reducing boilerplate code (using annotations like `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`).
* **Swagger/OpenAPI:** (e.g., `springdoc-openapi-starter-webmvc-ui`) For generating API documentation - (LMS only).
* **Validation API:** (e.g., `spring-boot-starter-validation`) For request body validation.
* **SuperBase:** To use the SuperBase database

### Example Maven `pom.xml` (LMS):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.x</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.example</groupId>
    <artifactId>lms</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>lms</name>
    <description>Loan Management System</description>
    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.integration</groupId>
            <artifactId>spring-integration-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.integration</groupId>
            <artifactId>spring-integration-ws</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
            <version>4.1.0</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.3.0</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>io.supabase</groupId>
            <artifactId>supabase-java</artifactId>
            <version>0.9.0</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### Example Maven `pom.xml` (Middleware):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.x</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.example</groupId>
    <artifactId>middleware</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>middleware</name>
    <description>Middleware for Transactional Data</description>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.integration</groupId>
            <artifactId>spring-integration-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.integration</groupId>
            <artifactId>spring-integration-ws</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## 3. Application Structure

Create the following basic package structure within your `src/main/java` directory:

```
com.example.lms (or com.example.middleware)
├── controller     # Contains REST controllers
├── service        # Contains business logic
├── repository     # Contains JPA repositories (for LMS only)
├── entity         # Contains JPA entities (for LMS only)
├── config         # Contains configuration classes
└── client         # Contains Feign clients (for LMS only)
```

## 4. Configuration

### `application.properties` or `application.yml`:

#### Database Configuration (LMS):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/lmsdb
spring.datasource.username=yourusername
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update

# Server port
server.port=8080  # for LMS
# server.port=8081  # for Middleware

# Swagger UI path
springdoc.swagger-ui.path=/swagger-ui.html
```

## 5. Enable Feign clients (LMS)

Add `@EnableFeignClients` annotation to your main application class (e.g., `LmsApplication.java`):

```java
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableFeignClients
public class LmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LmsApplication.class, args);
    }
}
```

## Key Points

* **Spring Initializr:** Provides a convenient way to create a new Spring Boot project with the necessary dependencies.
* **Dependencies:** I've listed the core dependencies you'll need for both the LMS and the Middleware, including `supabase-java`.
* **Application Structure:** A basic package structure to organize your code.
* **Configuration:** Example `application.properties` entries for database connection, server port, SuperBase URL and Key and Swagger.
* **`@EnableFeignClients`:** Important for enabling Feign clients in the LMS project.
* **Separate Projects:** You should create *two* separate Spring Boot projects: one for the LMS and one for the Middleware.

## Next Steps

1. Create two Spring Boot projects (LMS and Middleware) using Spring Initializr.
2. Add the dependencies listed above to your `pom.xml` files.
3. Create the basic package structure.
4. Configure your `application.properties` or `application.yml` files.
5. Add `@EnableFeignClients` to your LMS application class.

After you've completed these steps, we can move on to **Step 5: Develop API Endpoints and Business Logic (05_Business_Logic.md)**.
# Loan Management System (LMS)

A Spring Boot application for managing loan subscriptions and customer data with integration to Core Banking System (CBS) and Scoring Engine services.

## Project Overview

The Loan Management System (LMS) is designed to manage customer subscriptions, retrieve KYC data from a Core Banking System (CBS), and interact with a Scoring Engine for credit assessment. The application follows a microservices architecture pattern with RESTful APIs.

## Project Structure

```
src/main/
├── java/com/example/lms/
│   ├── client/            # Feign clients for external service communication
│   ├── config/            # Configuration classes
│   ├── controller/        # REST API controllers
│   ├── dto/               # Data Transfer Objects
│   ├── entity/            # JPA entities
│   ├── repository/        # Spring Data JPA repositories
│   ├── service/           # Business logic services
│   └── LmsApplication.java # Main application class
└── resources/
    ├── application.properties # Application configuration
    ├── static/               # Static resources
    └── templates/            # HTML templates (if any)
```

## Key Components

### Configuration

- **CBSConfig**: Configures integration with the Core Banking System using Spring Integration and Web Services.
- **ScoringEngineProperties**: Configuration properties for the Scoring Engine service.

### Controllers

- **SubscriptionController**: Handles customer subscription requests and interactions.

### Entities

- **Customer**: JPA entity representing customer data with UUID primary key and KYC information.

### Services

- **CustomerService**: Interface defining customer-related operations.
- **CustomerServiceImpl**: Implementation of CustomerService that handles customer subscriptions and CBS interactions.

### Integration

- The application uses Spring Integration for message-based communication with external systems.
- SOAP Web Services are used for CBS communication (currently mocked for development).

## Technologies Used

- **Spring Boot 3.2.3**: Application framework
- **Spring Data JPA**: Database access and ORM
- **Spring Integration**: Enterprise integration patterns
- **Spring Web Services**: SOAP web services support
- **H2 Database**: In-memory database for development
- **Lombok**: Reduces boilerplate code
- **Springdoc OpenAPI**: API documentation
- **Jakarta XML Binding**: XML processing for SOAP messages

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Running the Application

### Using Maven

```bash
# Clone the repository (if not already done)
git clone <repository-url>
cd lms

# Build and run the application
mvn clean spring-boot:run
```

### Using the JAR file

```bash
# Build the JAR file
mvn clean package

# Run the JAR file
java -jar target/lms-0.0.1-SNAPSHOT.jar
```

## Accessing the Application

- **Base URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: jdbc:h2:mem:lmsdb
  - Username: sa
  - Password: password

## API Endpoints

- **POST /api/subscriptions/{customerNumber}**: Subscribe a customer
- **GET /api/customers/{customerNumber}**: Get customer details by customer number

## Development Notes

- The CBS service calls are currently mocked to allow for development without external dependencies.
- The application uses an in-memory H2 database which resets on application restart.
- For production deployment, consider configuring a persistent database like PostgreSQL.

## Testing

```bash
# Run tests
mvn test
```

## Configuration

Key configuration properties in `application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:h2:mem:lmsdb
spring.datasource.username=sa
spring.datasource.password=password

# Server port
server.port=8080

# Scoring Engine Configuration
scoring.engine.url=http://localhost:8082
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

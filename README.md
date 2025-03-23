# Loan Management System (LMS) with Transaction Middleware

## Project Overview
This project consists of two main components:
1. **Loan Management System (LMS)** - A Spring Boot application for managing loan applications and customer data
2. **Transaction Middleware** - A service that provides transaction data for customers

## System Architecture

The system follows a microservices architecture with the following components:

### Loan Management System (LMS)
- Core application for loan processing and customer management
- Runs on port 8080
- Uses H2 in-memory database for development and testing
- Includes a mocked CBS (Core Banking System) service

### Transaction Middleware
- Provides transaction data for customers
- Runs on port 8081
- Includes mock services for scoring engine and core banking system
- Configurable with the "mock" Spring profile

## Deployment

Both applications are deployed on fly.io:
- LMS: https://lms-credible-assessment.fly.dev
- Middleware: https://middleware-credible-assessment.fly.dev

### Deployment Script
A PowerShell deployment script (`deploy-to-fly.ps1`) is available for updating the middleware on the fly.io platform. The script handles:

1. Verifying prerequisites (flyctl, Java, Maven)
2. Building the application with Maven
3. Verifying build artifacts
4. Checking fly.toml configuration
5. Authenticating with fly.io
6. Deploying to fly.io
7. Verifying deployment

To deploy:
```powershell
./deploy-to-fly.ps1
```

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven

### Running the Applications

#### Running the LMS
```bash
cd lms
./mvnw spring-boot:run
```

#### Running the Middleware
```bash
cd middleware
./mvnw spring-boot:run
```

To run with mock services enabled:
```bash
cd middleware
./mvnw spring-boot:run -Dspring.profiles.active=mock
```

## Key Features

### LMS Features
- Customer management (create, retrieve, update)
- Loan application processing
- Integration with scoring engine
- Subscription management

### Middleware Features
- Transaction data retrieval by customer number
- Mock transaction data generation for testing
- Configurable mock services for scoring engine and core banking

## API Endpoints

### LMS Endpoints
- `POST /subscriptions` - Create customer subscription
- `POST /loans` - Create loan request
- `POST /scores` - Initiate score query
- `GET /loans/{loanId}` - Query loan status

### Middleware Endpoints
- `GET /transactions/{customerNumber}` - Retrieves transaction data for a specific customer
- `GET /api/mock/status` - Check if mock services are active

## Testing

The project includes comprehensive test suites:

### Java-based Tests
Located in the `tests` directory, these tests cover both the LMS and Middleware APIs:

```bash
cd tests
mvn clean test
```

### PowerShell Test Scripts
For quick end-to-end testing:

- `test-apis.ps1` - Tests the complete loan application flow
- `test-scoring-engine.ps1` - Tests the scoring engine connection

See the [tests/README.md](tests/README.md) for detailed testing instructions.

## Documentation

Comprehensive documentation is available in the `markdown-documentation` directory:

1. [Requirements](markdown-documentation/01_Requirements.md)
2. [System Architecture](markdown-documentation/02_System_Architecture.md)
3. [API Endpoints](markdown-documentation/03_API_Endpoints.md)
4. [Spring Initialization](markdown-documentation/04_Spring_Init.md)
5. [Business Logic](markdown-documentation/05_Business_Logic.md)
6. [Database Design](markdown-documentation/06_Database_Design.md)
7. [Testing](markdown-documentation/07_Testing.md)
8. [Deployment](markdown-documentation/08_Deployment.md)
9. [Documentation](markdown-documentation/09_Documentation.md)
10. [Project Overview](markdown-documentation/project-overview.md)

## Development

### Project Structure

```
├── lms/                    # Loan Management System
│   ├── src/                # Source code
│   │   ├── main/           # Main application code
│   │   └── test/           # Test code
│   └── pom.xml             # Maven configuration
│
├── middleware/             # Transaction Middleware
│   ├── src/                # Source code
│   │   ├── main/           # Main application code
│   │   └── test/           # Test code
│   └── pom.xml             # Maven configuration
│
├── tests/                  # API Tests
│   ├── src/                # Test source code
│   ├── test-apis.ps1       # PowerShell test script
│   └── pom.xml             # Maven configuration
│
└── markdown-documentation/ # Project documentation
    ├── 01_Requirements.md
    ├── 02_System_Architecture.md
    └── ...
```

### Technology Stack
- Java 17
- Spring Boot 3.4.4
- Spring Data JPA
- H2 Database (for development)
- Maven
- JUnit and Mockito for testing
- Supabase for data storage

## Supabase Integration
The application integrates with Supabase for data storage:
- Project URL: https://wtacmsdiytqkgcwyrmhq.supabase.co
- API access is configured with the appropriate API key

## Future Enhancements
- Replace mock data with real data sources
- Add authentication and authorization
- Implement additional features for loan management
- Enhance transaction data analysis capabilities

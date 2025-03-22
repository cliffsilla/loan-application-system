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
- Currently returns mock transaction data for testing purposes

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

## Key Features

### LMS Features
- Customer management (create, retrieve, update)
- Loan application processing
- Integration with scoring engine
- Subscription management

### Middleware Features
- Transaction data retrieval by customer number
- Mock transaction data generation for testing

## API Endpoints

### LMS Endpoints
- Customer management endpoints
- Loan application endpoints
- Subscription management endpoints

### Middleware Endpoints
- `GET /transactions/{customerNumber}` - Retrieves transaction data for a specific customer

## Development

### Project Structure

```
├── lms/                    # Loan Management System
│   ├── src/                # Source code
│   │   ├── main/           # Main application code
│   │   └── test/           # Test code
│   └── pom.xml             # Maven configuration
│
└── middleware/             # Transaction Middleware
    ├── src/                # Source code
    │   ├── main/           # Main application code
    │   └── test/           # Test code
    └── pom.xml             # Maven configuration
```

### Technology Stack
- Java 17
- Spring Boot
- Spring Data JPA
- H2 Database (for development)
- Maven
- JUnit and Mockito for testing

## Testing
Both applications include unit tests and integration tests. Run the tests using Maven:

```bash
./mvnw test
```

## Future Enhancements
- Replace mock data with real data sources
- Add authentication and authorization
- Implement additional features for loan management
- Enhance transaction data analysis capabilities

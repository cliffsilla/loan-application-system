# Loan Application System Project Overview

## Project Overview

The Loan Application System is a microservices-based architecture designed to process loan applications. It consists of two main components:

1. **Loan Management System (LMS)**: Handles loan applications, customer subscriptions, and communicates with external systems
2. **Middleware**: Acts as an intermediary between the LMS and the Core Banking System (CBS), translating REST to SOAP

The system integrates with:
- **Core Banking System (CBS)**: Provides customer data and transaction history via SOAP APIs
- **Scoring Engine**: Evaluates loan applications based on customer data and transaction history

## Problem Solving Approach

The Loan Application System addresses several key challenges:

1. **Legacy System Integration**: Bridging modern REST APIs with legacy SOAP services through a dedicated middleware layer
2. **Asynchronous Processing**: Implementing asynchronous loan processing to handle potentially long-running credit scoring operations
3. **Resilience and Fault Tolerance**: Using retry mechanisms and circuit breakers to handle temporary service outages
4. **Mock Service Implementation**: Creating realistic mock services for development and testing when external systems are unavailable
5. **Deployment Automation**: Streamlining deployment to cloud platforms with automated scripts and configuration

## Data Structures

The system employs various data structures to efficiently manage loan application processing:

1. **Domain Models**:
   - Customer entity with validation constraints
   - Loan entity with state transitions (PENDING, APPROVED, REJECTED)
   - Transaction records with account information and amounts

2. **Data Transfer Objects (DTOs)**:
   - Request/Response objects for API communication
   - Mapping layer between external and internal representations

3. **Collections and Queues**:
   - Transaction lists for credit evaluation
   - Asynchronous processing queues for loan applications
   - Caching mechanisms for frequently accessed data

## Architectural Approaches

### Microservices Architecture

The system follows a microservices architecture with:

1. **Service Boundaries**: Clear separation between LMS and middleware responsibilities
2. **API Gateway Pattern**: Centralized entry points for mobile applications
3. **Circuit Breaker Pattern**: Preventing cascading failures when external systems are unavailable
4. **Adapter Pattern**: Converting between different interface formats (REST to SOAP)
5. **Repository Pattern**: Abstracting data access logic

### System Architecture

The system integrates with:
- **Core Banking System (CBS)**: Provides customer data and transaction history via SOAP APIs
- **Scoring Engine**: Evaluates loan applications based on customer data and transaction history

The system consists of two main components:

1. **Loan Management System (LMS)**: Handles loan applications, customer subscriptions, and communicates with external systems
2. **Middleware**: Acts as an intermediary between the LMS and the Core Banking System (CBS), translating REST to SOAP

## Optimization Techniques

The system incorporates several optimization strategies:

1. **Connection Pooling**: Reusing database and HTTP connections to reduce overhead
2. **Caching**: Implementing in-memory caches for frequently accessed customer and transaction data
3. **Timeout Tuning**: Optimized timeout settings to balance between responsiveness and reliability
4. **Asynchronous Processing**: Non-blocking operations for improved throughput
5. **Reduced Retry Attempts**: Optimized retry mechanisms (reduced from 5 to 2 attempts with shorter delays)
6. **Lazy Loading**: Loading related entities only when needed

## Experience with Integrations and Protocols

The project demonstrates expertise in various integration approaches:

1. **REST API Design**: Well-structured RESTful APIs with proper resource naming and HTTP methods
2. **SOAP Integration**: Spring Web Services for SOAP client implementation
3. **JSON Processing**: Serialization and deserialization of JSON payloads
4. **XML Handling**: Processing XML requests and responses with JAXB
5. **API Authentication**: Token-based authentication with JWT
6. **Content Negotiation**: Supporting multiple content types (JSON, XML)
7. **Error Handling**: Standardized error responses across integration points

## Java Skills Demonstrated

The project showcases advanced Java development skills:

1. **Spring Boot**: Building production-ready applications with minimal configuration
2. **Spring Data JPA**: Object-relational mapping and repository abstractions
3. **Spring Web Services**: SOAP client implementation
4. **Java 8+ Features**: Lambdas, Streams API, Optional, and CompletableFuture
5. **Exception Handling**: Custom exception hierarchies and global exception handling
6. **Dependency Injection**: Proper use of Spring's IoC container
7. **Testing**: JUnit, Mockito, and Spring Test frameworks
8. **Logging**: SLF4J with logback for comprehensive application logging

## DevOps Experience

The project incorporates DevOps practices for efficient development and deployment:

1. **Containerization**: Docker-based deployment with optimized Dockerfiles
2. **Cloud Deployment**: Automated deployment to fly.io cloud platform
3. **CI/CD Scripting**: PowerShell deployment scripts with error handling and verification
4. **Environment Configuration**: Profile-based configuration for different environments (dev, test, prod)
5. **Health Monitoring**: Endpoints for system health checks and monitoring
6. **Logging and Observability**: Structured logging for troubleshooting and monitoring
7. **Infrastructure as Code**: Configuration files for cloud deployment (fly.toml)

## Stakeholder Engagement

The project demonstrates effective stakeholder engagement through:

1. **Comprehensive Documentation**: Detailed markdown files explaining system architecture and processes
2. **API Documentation**: Clear endpoint descriptions with request/response examples
3. **Process Flows**: Visual and textual representations of business processes
4. **Testing Guides**: Instructions for verifying system functionality
5. **Deployment Guides**: Step-by-step deployment instructions
6. **Mock Services**: Enabling stakeholders to test without dependencies on external systems
7. **Feedback Loops**: Mechanisms for incorporating stakeholder feedback into the development process

## End-to-End Loan Process Flow

### 1. Customer Subscription
- **Endpoint**: `POST /subscriptions` (LMS)
- **Purpose**: Registers a customer with the LMS
- **Process**: The mobile application sends the customer number to the LMS, which registers the customer and returns a customer ID

### 2. Loan Request
- **Endpoint**: `POST /loans` (LMS)
- **Purpose**: Requests a loan for a subscribed customer
- **Process**: 
  - LMS receives the loan request with customer number and amount
  - LMS validates the customer and prevents multiple active loan applications
  - LMS retrieves customer KYC data from the CBS via SOAP
  - LMS initiates the score querying process

### 3. Retrieve Customer KYC Data
- **Process**: LMS communicates with CBS via SOAP to get customer information
- **Data Retrieved**: Customer details including name, income, and creation date

### 4. Initiate Score Query
- **Endpoint**: `POST /scores` (LMS)
- **Purpose**: Starts the process of querying the customer's credit score
- **Process**: LMS sends the customer number to the Scoring Engine, which returns a token for tracking

### 5. Retrieve Transaction Data
- **Endpoint**: `GET /transactions/{customerNumber}` (Middleware)
- **Purpose**: Scoring Engine retrieves transaction data for credit evaluation
- **Process**:
  - Scoring Engine calls the middleware with the customer number
  - Middleware retrieves transaction data from CBS via SOAP
  - Middleware returns transaction data in JSON format to the Scoring Engine

### 6. Query Loan Status
- **Endpoint**: `GET /loans/{loanId}` (LMS)
- **Purpose**: Retrieves the current status of a loan application
- **Process**: Mobile application queries the LMS for loan status, which returns details including status, amount, score, and limit

## Mock Services

The system includes mock implementations for testing and development:

1. **MockScoringEngineService**: Simulates the Scoring Engine with configurable responses
2. **MockCoreBankingService**: Simulates the Core Banking System for customer data and transactions
3. **MockServicesConfig**: Configuration class that activates with the "mock" Spring profile
4. **application-mock.properties**: Configuration file with mock-specific settings

These mock services provide realistic simulated responses with configurable success rates and delays to mimic real-world behavior.

## Deployment to fly.io

The deployment process is automated using PowerShell scripts:

1. **Prerequisites Check**:
   - Verifies flyctl, Java, and Maven are installed

2. **Build Process**:
   - Runs Maven to build the application
   - Verifies the JAR file was created

3. **Configuration Verification**:
   - Checks fly.toml configuration exists
   - Verifies authentication with fly.io

4. **Deployment**:
   - Deploys the application to fly.io
   - Verifies deployment success
   - Displays application URLs and health check endpoints

The applications are deployed at:
- Middleware: https://middleware-credible-assessment.fly.dev
- LMS: https://lms-credible-assessment.fly.dev

## Testing

The system includes comprehensive testing capabilities:

1. **API Testing Script**:
   - Tests the entire loan application flow
   - Uses real customer IDs and API keys
   - Includes retry logic for resilience

2. **Test Process**:
   - Customer subscription
   - Loan request creation
   - Score query initiation
   - Transaction data retrieval
   - Loan status verification

3. **Mock Profile Testing**:
   - Enables testing without external dependencies
   - Configurable success rates and response times
   - Toggle endpoints to check if mock services are active

## Security and Integration

The system uses:
- API key authentication for secure communication
- SOAP integration for legacy systems
- Proper error handling and validation
- Configurable timeouts and retry mechanisms

## Conclusion

The Loan Application System demonstrates a modern microservices architecture that bridges legacy SOAP services with modern REST APIs. It provides a complete solution for loan application processing, from customer subscription to loan approval, with comprehensive testing capabilities and automated deployment.

The project showcases advanced problem-solving skills, architectural expertise, optimization techniques, and experience with various integration protocols. It demonstrates proficiency in Java development, DevOps practices, and effective stakeholder engagement through comprehensive documentation and testing capabilities.

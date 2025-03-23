# API Tests for Loan Application System

This project contains automated tests for the Loan Application System APIs, including both the LMS (Loan Management System) and Middleware components.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PowerShell 5.1 or higher (for script-based tests)

## Project Structure

```
tests/
├── pom.xml                  # Maven configuration
├── test-apis.ps1           # PowerShell script for end-to-end API testing
├── test-scoring-engine.ps1 # PowerShell script for testing scoring engine connection
└── src/
    └── test/
        └── java/
            └── com/
                └── example/
                    └── tests/
                        ├── config/
                        │   └── TestConfig.java  # Configuration constants
                        ├── models/
                        │   ├── Customer.java    # Request/response models
                        │   ├── LoanRequest.java
                        │   ├── LoanResponse.java
                        │   ├── ScoreRequest.java
                        │   ├── ScoreResponse.java
                        │   └── Transaction.java
                        ├── BaseApiTest.java     # Base test class with common setup
                        ├── LmsApiTest.java      # Tests for LMS API endpoints
                        ├── MiddlewareApiTest.java # Tests for Middleware API endpoints
                        └── EndToEndLoanProcessTest.java # End-to-end process flow test
```

## Configuration

Before running the tests, update the `TestConfig.java` file with the appropriate values:

1. The base URLs are already set to the deployed applications:
   - LMS: https://lms-credible-assessment.fly.dev
   - Middleware: https://middleware-credible-assessment.fly.dev

2. Replace the placeholder API key with your actual API key.

3. Optionally, update the test data (customer number, loan amount) if needed.

## Running the Tests

### Running All Tests

To run all tests, use the following command from the `tests` directory:

```bash
mvn clean test
```

### Running Specific Test Classes

To run a specific test class, use:

```bash
mvn clean test -Dtest=LmsApiTest
```

Replace `LmsApiTest` with the name of the test class you want to run.

### Running the End-to-End Test

The `EndToEndLoanProcessTest` simulates the complete loan application process flow as described in the documentation:

```bash
mvn clean test -Dtest=EndToEndLoanProcessTest
```

### Using PowerShell Test Scripts

In addition to the Java-based tests, this project includes PowerShell scripts for testing:

#### End-to-End API Test Script

The `test-apis.ps1` script tests the complete loan application flow from customer subscription to loan status retrieval:

```powershell
.\test-apis.ps1
```

This script performs the following steps:
1. Customer Subscription
2. Loan Request
3. Score Query
4. Transaction Data Retrieval
5. Loan Status Query

The script includes retry logic and detailed logging to help diagnose any issues.

#### Scoring Engine Connection Test

The `test-scoring-engine.ps1` script specifically tests the connection to the scoring engine:

```powershell
.\test-scoring-engine.ps1
```

This script verifies that the middleware can connect to the scoring engine and fall back to mock services when needed.

## Test Coverage

The tests cover the following API endpoints:

### LMS API

1. Customer Subscription (`POST /subscriptions`)
2. Loan Request (`POST /loans`)
3. Initiate Score Query (`POST /scores`)
4. Query Loan Status (`GET /loans/{loanId}`)

### Middleware API

1. Retrieve Transaction Data (`GET /transactions/{customerNumber}`)
2. Mock Services Status (`GET /api/mock/status`) - PowerShell script only

## Notes

- The tests are designed to run in sequence, with dependencies between them (e.g., creating a customer before requesting a loan).
- The `EndToEndLoanProcessTest` includes a delay to allow time for the scoring process to complete.
- Error scenarios are also tested, such as requesting non-existent loans or customers.
- The tests use RestAssured for API testing and AssertJ for assertions.
- The PowerShell scripts provide an alternative testing approach that doesn't require a Java development environment.
- When the real services are unavailable, the middleware will automatically fall back to using mock services if the mock profile is active.

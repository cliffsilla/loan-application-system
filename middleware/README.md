# Transaction Middleware Service

## Overview
This middleware service provides transaction data for the Loan Management System (LMS). It acts as a bridge between the LMS and external transaction data sources, offering a simplified API to retrieve customer transaction information.

## Features
- Retrieves transaction data for customers by customer number
- Currently provides mock transaction data for testing and development purposes
- Runs on a separate port (8081) to avoid conflicts with the LMS application

## Technical Stack
- Java 17
- Spring Boot
- Maven

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven

### Running the Application

#### Using Maven Wrapper
```bash
./mvnw spring-boot:run
```

#### Using Maven
```bash
mvn spring-boot:run
```

The application will start on port 8081 as configured in `application.properties`.

## API Endpoints

### Get Transaction Data

**Endpoint:** `GET /transactions/{customerNumber}`

**Description:** Retrieves transaction data for a specific customer

**Path Parameters:**
- `customerNumber` - The unique identifier for the customer

**Sample Request:**
```
GET http://localhost:8081/transactions/12345
```

**Sample Response:**
```json
{
  "count": 5,
  "customerNumber": "12345",
  "transactions": [
    {
      "date": "2025-03-01",
      "amount": 1500.0,
      "description": "Supermarket",
      "id": "TXN001",
      "type": "DEBIT"
    },
    {
      "date": "2025-03-05",
      "amount": 5000.0,
      "description": "Salary",
      "id": "TXN002",
      "type": "CREDIT"
    },
    {
      "date": "2025-03-10",
      "amount": 2000.0,
      "description": "Rent",
      "id": "TXN003",
      "type": "DEBIT"
    },
    {
      "date": "2025-03-15",
      "amount": 500.0,
      "description": "Restaurant",
      "id": "TXN004",
      "type": "DEBIT"
    },
    {
      "date": "2025-03-18",
      "amount": 300.0,
      "description": "Utilities",
      "id": "TXN005",
      "type": "DEBIT"
    }
  ]
}
```

## Development

### Project Structure
- `controller` - REST API endpoints
- `service` - Business logic and data processing

### Adding New Endpoints
To add new endpoints, create a new controller class or extend the existing `TransactionDataController` with additional methods.

### Connecting to Real Data Sources
Currently, the service returns mock data. To connect to real data sources:
1. Create appropriate repository interfaces and implementations
2. Update the service layer to use these repositories
3. Configure database connections in `application.properties`

## Integration with LMS
The middleware is designed to be consumed by the Loan Management System (LMS). The LMS can make HTTP requests to this middleware to retrieve transaction data for customers, which can then be used for loan processing, credit scoring, or reporting purposes.

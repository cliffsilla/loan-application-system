## Loan Management Module (LMS) Requirements

This document outlines the functional and non-functional requirements for the Loan Management Module (LMS), which integrates with the Bank's CORE Banking System (CBS) and the Scoring Engine.

### 1. Functional Requirements

*   **1.1. Customer Subscription:**
    *   The LMS must provide a REST API endpoint for the mobile application to subscribe a customer.
    *   The API must accept a customer number as input.
    *   The LMS must store the customer information (KYC data) retrieved from the CBS.

*   **1.2. Loan Application:**
    *   The LMS must provide a REST API endpoint for the mobile application to submit a loan application.
    *   The API must accept the customer number and loan amount as input.
    *   The LMS must validate that the customer is subscribed.
    *   The LMS must prevent multiple loan applications from the same customer if there is an ongoing loan request.
    *   The LMS must retrieve customer KYC data from the CBS.
    *   The LMS must integrate with the Scoring Engine to obtain a credit score and loan limit for the customer.
    *   The LMS must store the loan application details, including the customer number, loan amount, credit score, and loan limit.

*   **1.3. Loan Status:**
    *   The LMS must provide a REST API endpoint for the mobile application to query the status of a loan application.
    *   The API must accept a loan ID as input.
    *   The LMS must return the loan status (e.g., PENDING, APPROVED, REJECTED, ACTIVE, COMPLETED), loan amount, credit score, loan limit, and rejection reason (if applicable).

*   **1.4. CORE Banking System (CBS) Integration:**
    *   The LMS must integrate with the CBS via SOAP APIs to:
        *   Retrieve customer KYC data using the KYC API.
        *   Retrieve customer transactional data using the Transactions Data API (via Middleware).

*   **1.5. Scoring Engine Integration:**
    *   The LMS must integrate with the Scoring Engine via REST APIs to:
        *   Initiate a score query, providing the customer number and receiving a token.
        *   Query the score using the token, receiving the credit score and loan limit.
    *   The LMS must implement a retry mechanism for querying the score from the Scoring Engine. The number of retries should be configurable.
    *   The LMS must handle scenarios where the Scoring Engine does not respond after multiple retries, failing the loan application.
    *   The LMS must expose a REST API for the Scoring Engine to retrieve customer transactional data (via Middleware).

*   **1.6. Middleware Functionality:**
    *   The Middleware must provide a REST API endpoint for the Scoring Engine to retrieve customer transactional data.
    *   The Middleware must integrate with the CBS via SOAP API to retrieve customer transactional data.
    *   The Middleware must transform the data from the CBS format to the format expected by the Scoring Engine (if necessary).

### 2. Non-Functional Requirements

*   **2.1. Security:**
    *   The LMS REST APIs must be secured using API keys or OAuth 2.0.
    *   The callback URL registration with the Scoring Engine must be secured with Basic Authentication.
*   **2.2. Performance:**
    *   The LMS must be responsive and handle a reasonable number of concurrent requests.
    *   The retry mechanism for the Scoring Engine must not significantly impact the overall performance of the loan application process.
*   **2.3. Reliability:**
    *   The LMS must be reliable and handle failures gracefully.
    *   The retry mechanism for the Scoring Engine must ensure that loan applications are not lost due to temporary network issues or Scoring Engine unavailability.
*   **2.4. API Documentation:**
    *   The LMS REST APIs must be well-documented using Swagger/OpenAPI.
*   **2.5. API documentation should be provided for the three mobile application calls.**

### 3. Data Flow

*   Mobile App -> LMS -> CBS (KYC Data)
*   Mobile App -> LMS -> Scoring Engine (Score and Limit)
*   Scoring Engine -> Middleware -> CBS (Transactional Data)
*   LMS -> Mobile App (Loan Status)

### 4. Error Handling

*   The LMS must handle errors gracefully and provide informative error messages to the mobile application.
*   The LMS must log all errors and exceptions.
*   The retry mechanism for the Scoring Engine must handle different types of errors (e.g., network errors, server errors).

### 5. Provided Components (Out of Scope)

*   Mobile Application
*   CORE Banking System (CBS)
*   Scoring Engine
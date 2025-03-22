## Component Responsibilities

**1. LMS (Loan Management System)**

*   **Description:** The core service responsible for managing loan applications, interacting with the CBS for customer KYC data, and integrating with the Scoring Engine for credit scoring.
*   **Responsibilities:**
    *   **REST API Endpoints (for Mobile App):**
        *   `/subscriptions` - Handles customer subscription requests.
        *   `/loans` - Handles loan application requests.
        *   `/loans/{loanId}` - Handles loan status requests.
    *   **REST API Endpoint (for Scoring Engine):**
        *   `/scores` -  Starts the process of querying the score from the Scoring Engine
    *   **Core Logic:**
        *   Receives and processes customer subscription requests.
        *   Receives and processes loan application requests:
            *   Calls CBS to retrieve Customer KYC Data (SOAP).
            *   Stores Customer KYC Data in the LMS database.
            *   Calls Scoring Engine to initiate score query (REST).
            *   Calls Scoring Engine to retrieve score (REST).
        *   Handles loan status requests.
        *   Implements retry logic for communicating with the Scoring Engine.
*   **Technology:**
    *   Language: Java
    *   Framework: Spring Boot
    *   Database: PostgreSQL (for storing Customer KYC data)
    *   REST Client: Feign Client
    *   SOAP Client: Spring Integration
    *   API Documentation: Swagger

**2. Middleware**

*   **Description:** A separate service that acts as an intermediary between the Scoring Engine and the CBS for retrieving transactional data.
*   **Responsibilities:**
    *   **REST API Endpoint (for Scoring Engine):**
        *   `/transactions/{customerNumber}` - Receives requests for transactional data from the Scoring Engine.
    *   **Core Logic:**
        *   Receives requests for transactional data from the Scoring Engine (REST).
        *   Calls CBS to retrieve transactional data (SOAP).
        *   Transforms the SOAP response to the format required by the Scoring Engine (if necessary).
        *   Returns the transactional data to the Scoring Engine.
*   **Technology:**
    *   Language: Java
    *   Framework: Spring Boot
    *   REST API: Spring Web
    *   SOAP Client: Spring Integration

**3. Supabase (Optional)**

*   **Description:** A Backend-as-a-Service (BaaS) platform that can be used for user authentication, database storage (alternative to PostgreSQL for some data), and other backend functionalities.  This is *optional* and adds complexity.
*   **Responsibilities (If Used):**
    *   User Authentication (handling user registration, login, and session management).
    *   Potentially storing user-related data (e.g., user profiles, loan application history).
*   **Technology:**
    *   Supabase Java client (if directly integrating from the LMS).
    *   REST API calls to Supabase (alternative).

**4. Provided Components (Out of Scope)**

*   **Mobile Application:** The bank's existing mobile application.
*   **CORE Banking System (CBS):** The bank's core banking system.
*   **Scoring Engine:** The scoring company's credit scoring engine.

## Technology Stack Summary

*   **Language:** Java
*   **Framework:** Spring Boot
*   **Database:** PostgreSQL (LMS only - for KYC)
*   **REST Client:** Feign Client (LMS to Scoring Engine)
*   **SOAP Client:** Spring Integration (LMS and Middleware to CBS)
*   **Build Tool:** Maven
*   **API Documentation:** Swagger (for LMS REST APIs)
*   **Authentication (Optional):** Supabase (or Spring Security, or a custom solution)

## Notes

*   This architecture now includes Feign for REST client communication, Swagger for API documentation, and an optional integration with Supabase.
*   Supabase can be used for authentication and potentially for storing user-related data. If you do not want to manage authentication you can use Superbase
*   Spring Integration will be used for handling SOAP calls to the CBS from both the LMS and the Middleware.
*   The Middleware is designed as a separate service to isolate the CBS integration logic and potential data transformations.
*   Error handling and logging should be implemented in both the LMS and Middleware for robust operation.
*   If using Supabase, you'll need to choose a Supabase Java client library and configure it appropriately.
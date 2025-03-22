# API Endpoints for LMS and Middleware

This document defines the API endpoints for the Loan Management Module (LMS) and the Middleware, including request and response formats and headers using OpenAPI (Swagger) specification.

## 1. LMS API Endpoints

These endpoints are exposed by the LMS to the Mobile Application and the Scoring Engine.

### 1.1. Subscription API (`/subscriptions`)

* **Method:** `POST`
* **Description:** Subscribes a customer to the LMS.
* **Request Headers:**
  * `Content-Type`: `application/json`
  * `Authorization`: `API Key` (or `Bearer <OAuth 2.0 token>`)
* **Request Body (application/json):**

```json
{
  "customerNumber": "string"
}
```

* **Response (application/json):**
  * **Success (200 OK):**
    * **Headers:**
      * `Content-Type`: `application/json`

```json
{
  "customerId": "UUID"
}
```

  * **Error (400 Bad Request):**
    * **Headers:**
      * `Content-Type`: `application/json`

```json
{
  "error": "Customer already subscribed"
}
```

### 1.2. Loan Request API (`/loans`)

* **Method:** `POST`
* **Description:** Requests a loan for a customer.
* **Request Headers:**
  * `Content-Type`: `application/json`
  * `Authorization`: `API Key` (or `Bearer <OAuth 2.0 token>`)
* **Request Body (application/json):**

```json
{
  "customerNumber": "string",
  "amount": "number"
}
```

* **Response (application/json):**
  * **Success (201 Created):**
    * **Headers:**
      * `Content-Type`: `application/json`

```json
{
  "loanId": "UUID",
  "status": "PENDING"
}
```

  * **Error (400 Bad Request):**
    * **Headers:**
      * `Content-Type`: `application/json`

```json
{
  "error": "Customer has existing loan"
}
```

### 1.3. Loan Status API (`/loans/{loanId}`)

* **Method:** `GET`
* **Description:** Retrieves the status of a loan.
* **Path Parameter:**
  * `loanId`: UUID of the loan.
* **Request Headers:**
  * `Authorization`: `API Key` (or `Bearer <OAuth 2.0 token>`)
* **Response (application/json):**
  * **Success (200 OK):**
    * **Headers:**
      * `Content-Type`: `application/json`

```json
{
  "loanId": "UUID",
  "status": "string", // PENDING, APPROVED, REJECTED, ACTIVE, COMPLETED
  "amount": "number",
  "score": "number",
  "limit": "number",
  "rejectionReason": "string" // if rejected
}
```

  * **Error (404 Not Found):**
    * **Headers:**
      * `Content-Type`: `application/json`

```json
{
  "error": "Loan not found"
}
```

### 1.4. Initiate Score Query API (`/scores`)

* **Method:** `POST`
* **Description:** Initiates the process of querying the score from the Scoring Engine.
* **Request Headers:**
  * `Content-Type`: `application/json`
  * `Authorization`: `API Key` (or `Bearer <OAuth 2.0 token>`)
* **Request Body (application/json):**

```json
{
  "customerNumber": "string"
}
```

* **Response (application/json):**
  * **Success (200 OK):**
    * **Headers:**
      * `Content-Type`: `application/json`

```json
{
  "token": "string" // Token to be used to retrieve the score
}
```

  * **Error (400 Bad Request):**
    * **Headers:**
      * `Content-Type`: `application/json`

```json
{
  "error": "Invalid customer number"
}
```

### 1.5. Scoring Engine Callback API (`/scoring/callback`)

* **Method:** `POST`
* **Description:** Scoring engine registers itself to the LMS.
* **Request Headers:**
  * `Content-Type`: `application/json`
  * `Authorization`: `Basic <Base64 encoded username:password>`
* **Request Body (application/json):**

```json
{
  "url": "[YOUR ENDPOINT URL]",
  "name": "[NAME OF YOUR SERVICE]",
  "username": "[YOUR BASIC AUTHENTICATION USERNAME]",
  "password": "[YOUR BASIC AUTHENTICATION PASSWORD]"
}
```

* **Response (application/json):**
  * **Success (200 OK):**
    * **Headers:**
      * `Content-Type`: `application/json`

```json
{
  "id": 0, // generated client id
  "url": "[YOUR ENDPOINT]",
  "name": "[NAME OF YOUR SERVICE]",
  "username": "[YOUR BASIC AUTHENTICATION USERNAME]",
  "password": "[YOUR BASIC AUTHENTICATION PASSWORD]",
  "token": "[GENERATED UNIQUE UUID]" // use this to make call for scoring
}
```

## 2. Middleware API Endpoints

These endpoints are exposed by the Middleware to the Scoring Engine.

### 2.1. Transaction Data API (`/transactions/{customerNumber}`)

* **Method:** `GET`
* **Description:** Retrieves transaction data for a customer.
* **Path Parameter:**
  * `customerNumber`: The customer number.
* **Request Headers:**
  * `Authorization`: `API Key`
* **Response (application/json):**
  * **Success (200 OK):**
    * **Headers:**
      * `Content-Type`: `application/json`

```json
[
  {
    "accountNumber": "string",
    "transactionDate": "string",
    "amount": "number",
    // ... other transaction details
  },
  {
    "accountNumber": "string",
    "transactionDate": "string",
    "amount": "number",
    // ... other transaction details
  }
]
```

  * **Error (404 Not Found):**
    * **Headers:**
      * `Content-Type`: `application/json`

```json
{
  "error": "Customer not found or no transactions available"
}
```

## 3. Authentication and Authorization

* **LMS APIs (Mobile App):** API Key or OAuth 2.0 (implementation details to be defined).
* **LMS API (/scoring/callback - Scoring Engine):** Basic Authentication.
* **Middleware API (Scoring Engine):** API Key or other secure mechanism (implementation details to be defined).

## 4. Notes

* All API endpoints should use appropriate HTTP status codes to indicate success or failure.
* Request and response bodies should be validated to ensure data integrity.
* Error responses should include informative error messages to help with debugging.
* This document provides a starting point for API design and can be further refined as needed.
* The "Authorization" header is included in most requests. Replace placeholders with the proper authentication type (API Key or Bearer Token)

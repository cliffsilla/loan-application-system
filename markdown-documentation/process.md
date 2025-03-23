# End-to-End Loan Process Flow

This document outlines the entire flow for a customer loan application process, from subscription to loan status retrieval, including API endpoints, request payloads, and response formats.

## 1. Customer Subscription

* **Purpose:** Registers a customer with the LMS.
* **Actor:** Mobile Application
* **Endpoint:** `POST /subscriptions` (LMS)
* **Request Headers:**
  * `Content-Type`: `application/json`
  * `Authorization`: `API Key` (or `Bearer <OAuth 2.0 token>`)
* **Request Body (application/json):**

```json
{
  "customerNumber": "1234567890"
}
```

* **Response (application/json):**

  * **Success (200 OK):**

  ```json
  {
    "customerId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
  }
  ```

  * **Error (400 Bad Request):**

  ```json
  {
    "error": "Customer already subscribed"
  }
  ```

## 2. Loan Request

* **Purpose:** Requests a loan for a subscribed customer.
* **Actor:** Mobile Application
* **Endpoint:** `POST /loans` (LMS)
* **Request Headers:**
  * `Content-Type`: `application/json`
  * `Authorization`: `API Key` (or `Bearer <OAuth 2.0 token>`)
* **Request Body (application/json):**

```json
{
  "customerNumber": "1234567890",
  "amount": 1000.00
}
```

* **Process:**
  * LMS receives the loan request.
  * LMS validates the customer and prevents the customer from applying another loan, if there is one in process.
  * LMS retrieves customer KYC data from the CBS (using SOAP - see Step 3).
  * LMS initiates the score querying process.
* **Response (application/json):**

  * **Success (201 Created):**

  ```json
  {
    "loanId": "f1e2d3c4-b5a6-7890-1234-567890abcdef",
    "status": "PENDING"
  }
  ```

  * **Error (400 Bad Request):**

  ```json
  {
    "error": "Customer not found or invalid amount"
  }
  ```

## 3. Retrieve Customer KYC Data (LMS <-> CBS)

* **Purpose:** Retrieve customer KYC (Know Your Customer) data from the CORE Banking System.
* **Actor:** LMS
* **API:** SOAP (CBS)
* **Operation:** Customer (as defined in customerWsdl)
* **Request (SOAP Envelope):**

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:cus="http://credable.io/cbs/customer">
   <soapenv:Header/>
   <soapenv:Body>
      <cus:CustomerRequest>
         <cus:customerNumber>1234567890</cus:customerNumber>
      </cus:CustomerRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

* **Response (SOAP Envelope):**

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:cus="http://credable.io/cbs/customer">
   <soapenv:Header/>
   <soapenv:Body>
      <cus:CustomerResponse>
         <cus:customer>
            <cus:createdAt>2024-01-27T10:00:00</cus:createdAt>
            <cus:customerNumber>1234567890</cus:customerNumber>
            <cus:firstName>John</cus:firstName>
            <cus:lastName>Doe</cus:lastName>
            <cus:monthlyIncome>5000.0</cus:monthlyIncome>
         </cus:customer>
      </cus:CustomerResponse>
   </soapenv:Body>
</soapenv:Envelope>
```

## 4. Initiate Score Query (LMS -> Scoring Engine)

* **Purpose:** Initiates the process of querying the customer's credit score from the Scoring Engine.
* **Actor:** LMS
* **Endpoint:** `POST /scores` (LMS)
* **Request Headers:**
  * `Content-Type`: `application/json`
  * `Authorization`: `API Key` (or `Bearer <OAuth 2.0 token>`)
* **Request Body (application/json):**

```json
{
  "customerNumber": "1234567890"
}
```

* **Response (application/json):**

  * **Success (200 OK):**

  ```json
  {
    "token": "unique_token_123"
  }
  ```

  * **Error (400 Bad Request):**

  ```json
  {
    "error": "Invalid customer number"
  }
  ```

## 5. Retrieve Transaction Data (Scoring Engine -> Middleware -> CBS)

* **Purpose:** Scoring Engine retrieves the transactional data for a user with the token
* **Actor:** Scoring Engine
* **Endpoint:** `/transactions/{customerNumber}` (Middleware)
* **Request Headers:**
  * `Authorization`: `API Key`
* **Path Parameter:**
  * `customerNumber`: The customer number.
* **Process:**
  * Middleware retrieves the transaction data by calling to the CBS
  * **API:** SOAP (CBS)
  * **Operation:** Transactions (as defined in transactionWsdl)
  * **Request (SOAP Envelope):**

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tran="http://credable.io/cbs/transaction">
   <soapenv:Header/>
   <soapenv:Body>
      <tran:TransactionsRequest>
         <tran:customerNumber>1234567890</tran:customerNumber>
      </tran:TransactionsRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

* **Response (SOAP Envelope):**

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tran="http://credable.io/cbs/transaction">
   <soapenv:Header/>
   <soapenv:Body>
      <tran:TransactionsResponse>
         <tran:transactions>
            <tran:accountNumber>12345</tran:accountNumber>
            <tran:transactionDate>2024-01-01</tran:transactionDate>
            <tran:amount>100.0</tran:amount>
         </tran:transactions>
      </tran:TransactionsResponse>
   </soapenv:Body>
</soapenv:Envelope>
```

* **Response (application/json): (Middleware -> Scoring Engine)**

```json
[
  {
    "accountNumber": "12345",
    "transactionDate": "2024-01-01",
    "amount": 100.0
  }
]
```

## 6. Query Loan Status

* **Purpose:** Retrieves the status of a loan.
* **Actor:** Mobile Application
* **Endpoint:** `GET /loans/{loanId}` (LMS)
* **Request Headers:**
  * `Authorization`: `API Key` (or `Bearer <OAuth 2.0 token>`)
* **Path Parameter:**
  * `loanId`: f1e2d3c4-b5a6-7890-1234-567890abcdef
* **Response (application/json):**

  * **Success (200 OK):**

  ```json
  {
    "loanId": "f1e2d3c4-b5a6-7890-1234-567890abcdef",
    "status": "PENDING",
    "amount": 1000.00,
    "score": 650,
    "limit": 5000.00,
    "rejectionReason": null
  }
  ```

  * **Error (404 Not Found):**

  ```json
  {
    "error": "Loan not found"
  }
  ```

## Important Notes

* **Error Handling:** This document only shows success scenarios. Proper error handling and validation should be implemented.
* **Authentication/Authorization:** The Authorization headers are placeholders. You need to implement the actual authentication and authorization logic.
* **SOAP Integration:** The SOAP requests and responses are examples. You need to use Spring Integration to generate and parse the actual SOAP messages.
* **Placeholders:** Replace all placeholders (URLs, API keys, database credentials) with your actual values.
* **Security:** This document does not cover security aspects. Implement appropriate security measures to protect your APIs and data.
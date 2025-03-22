# Developing API Endpoints and Business Logic

This document outlines the steps to develop the API endpoints and business logic for the Loan Management Module (LMS) and the Middleware.

## 1. LMS API Endpoints and Business Logic

### 1.1. Subscription API (`/subscriptions`)

#### Controller (`SubscriptionController.java`)

```java
package com.example.lms.controller;

import com.example.lms.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

    @Autowired
    private CustomerService customerService;

    @PostMapping
    public ResponseEntity<?> subscribeCustomer(@RequestBody SubscriptionRequest request) {
        try {
            UUID customerId = customerService.subscribeCustomer(request.getCustomerNumber());
            return ResponseEntity.ok(new SubscriptionResponse(customerId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }

    // Request and Response DTOs
    static class SubscriptionRequest {
        private String customerNumber;

        public String getCustomerNumber() {
            return customerNumber;
        }

        public void setCustomerNumber(String customerNumber) {
            this.customerNumber = customerNumber;
        }
    }

    static class SubscriptionResponse {
        private UUID customerId;

        public SubscriptionResponse(UUID customerId) {
            this.customerId = customerId;
        }

        public UUID getCustomerId() {
            return customerId;
        }

        public void setCustomerId(UUID customerId) {
            this.customerId = customerId;
        }
    }

    static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
```

#### Service (`CustomerService.java`)

```java
package com.example.lms.service;

import com.example.lms.client.CBSClient;
import com.example.lms.entity.Customer;
import com.example.lms.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CBSClient cbsClient; // Feign client for CBS

    public UUID subscribeCustomer(String customerNumber) {
        // 1. Check if customer already exists
        if (customerRepository.findByCustomerNumber(customerNumber).isPresent()) {
            throw new RuntimeException("Customer already subscribed");
        }

        // 2. Call CBS to get KYC data (using Feign client) - Example
        //CBSSoapResponse kycData = cbsClient.getKYCData(customerNumber);

        // 3. Store customer data (including KYC data)
        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setCustomerNumber(customerNumber);
        //customer.setKycData(kycData); // Store KYC data
        customerRepository.save(customer);

        return customer.getCustomerId();
    }
}
```

#### Feign Client (`CBSClient.java`)

```java
package com.example.lms.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cbs-client", url = "YOUR_CBS_SOAP_ENDPOINT")
public interface CBSClient {

    @GetMapping("/kyc/{customerNumber}")
    String getKYCData(@PathVariable("customerNumber") String customerNumber);
}
```

> **NOTE:** The current implementation above would work for REST endpoint to get the KYC details. To make the SOAP client work, it would require a different approach.

### 1.2. Loan Request API (`/loans`)

#### Controller (`LoanController.java`)

```java
package com.example.lms.controller;

import com.example.lms.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @PostMapping
    public ResponseEntity<?> requestLoan(@RequestBody LoanRequest request) {
        try {
            UUID loanId = loanService.requestLoan(request.getCustomerNumber(), request.getAmount());
            return ResponseEntity.status(HttpStatus.CREATED).body(new LoanResponse(loanId, "PENDING"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }

    // Request and Response DTOs
    static class LoanRequest {
        private String customerNumber;
        private double amount;

        // Getters and setters
        public String getCustomerNumber() {
            return customerNumber;
        }

        public void setCustomerNumber(String customerNumber) {
            this.customerNumber = customerNumber;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }

    static class LoanResponse {
        private UUID loanId;
        private String status;

        public LoanResponse(UUID loanId, String status) {
            this.loanId = loanId;
            this.status = status;
        }

        public UUID getLoanId() {
            return loanId;
        }

        public void setLoanId(UUID loanId) {
            this.loanId = loanId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
```

#### Service (`LoanService.java`)

```java
package com.example.lms.service;

import com.example.lms.client.ScoringEngineClient;
import com.example.lms.entity.Loan;
import com.example.lms.entity.Customer;
import com.example.lms.repository.LoanRepository;
import com.example.lms.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.Optional;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ScoringEngineClient scoringEngineClient;

    public UUID requestLoan(String customerNumber, double amount) {
        // 1. Check if customer is subscribed
        Optional<Customer> customerOptional = customerRepository.findByCustomerNumber(customerNumber);
        if (customerOptional.isEmpty()) {
            throw new RuntimeException("Customer not subscribed");
        }
        Customer customer = customerOptional.get();

        // 2. Check for existing pending loans (Implement this logic)
        //if (loanRepository.existsByCustomerAndStatus(customer, "PENDING")) {
        //    throw new RuntimeException("Customer has existing pending loan");
        //}

        // 3. Call Scoring Engine to get score (using Feign client)
        String token = scoringEngineClient.initiateScoreQuery(customerNumber);
        // TODO: Implement logic to query the score with retry mechanism
        //double score = scoringEngineClient.getScore(token);

        //double limit = 50000; // Get the Limit
        // 4. Create and save loan
        Loan loan = new Loan();
        loan.setLoanId(UUID.randomUUID());
        loan.setCustomer(customer);
        loan.setAmount(amount);
        loan.setStatus("PENDING");
        //loan.setScore(score);
        //loan.setLimit(limit);
        loanRepository.save(loan);

        return loan.getLoanId();
    }
}
```

#### ScoringEngineClient

```java
package com.example.lms.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "scoring-engine-client", url = "YOUR_SCORING_ENGINE_ENDPOINT")
public interface ScoringEngineClient {

    @PostMapping(value = "/initiateScoreQuery", consumes = "application/json")
    String initiateScoreQuery(@RequestBody String customerNumber);

    @GetMapping("/queryScore/{token}")
    Double getScore(@PathVariable("token") String token);
}
```

### 1.3. Loan Status API (`/loans/{loanId}`)

#### Controller (`LoanController.java` - Add to existing)

```java
@GetMapping("/{loanId}")
public ResponseEntity<?> getLoanStatus(@PathVariable("loanId") UUID loanId) {
    try {
        LoanService.LoanStatusResponse loanStatus = loanService.getLoanStatus(loanId);
        return ResponseEntity.ok(loanStatus);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
    }
}
```

#### Service (`LoanService.java` - Add to existing)

```java
public LoanStatusResponse getLoanStatus(UUID loanId) {
    Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found"));

    // Map Loan entity to LoanStatusResponse DTO
    return new LoanStatusResponse(loan.getLoanId(), loan.getStatus(), loan.getAmount(), loan.getScore(), loan.getLimit(), loan.getRejectionReason());
}

static class LoanStatusResponse {
    private UUID loanId;
    private String status;
    private double amount;
    private double score;
    private double limit;
    private String rejectionReason;

    public LoanStatusResponse(UUID loanId, String status, double amount, double score, double limit, String rejectionReason) {
        this.loanId = loanId;
        this.status = status;
        this.amount = amount;
        this.score = score;
        this.limit = limit;
        this.rejectionReason = rejectionReason;
    }

    public UUID getLoanId() {
        return loanId;
    }

    public void setLoanId(UUID loanId) {
        this.loanId = loanId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
```

### 1.4 Scoring Engine callback API (`/scoring/callback`)

#### Controller (`ScoringCallbackController.java`)

```java
package com.example.lms.controller;

import com.example.lms.service.ScoringCallbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scoring")
public class ScoringCallbackController {

    @Autowired
    private ScoringCallbackService scoringCallbackService;

    @PostMapping("/callback")
    public ResponseEntity<?> registerScoringEngine(@RequestBody ScoringEngineRegistrationRequest request) {
        try {
            ScoringCallbackService.ScoringEngineRegistrationResponse registrationResponse = scoringCallbackService.registerScoringEngine(request.getUrl(), request.getName(), request.getUsername(), request.getPassword());
            return ResponseEntity.ok(registrationResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }

    static class ScoringEngineRegistrationRequest {
        private String url;
        private String name;
        private String username;
        private String password;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
```

#### Service (`ScoringCallbackService.java`)

```java
package com.example.lms.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ScoringCallbackService {

    public ScoringEngineRegistrationResponse registerScoringEngine(String url, String name, String username, String password) {
        // Logic to register the scoring engine (e.g., store in database)
        // Generate a unique token for the scoring engine
        String token = UUID.randomUUID().toString();

        return new ScoringEngineRegistrationResponse(0, url, name, username, password, token);
    }

    public static class ScoringEngineRegistrationResponse {
        private int id;
        private String url;
        private String name;
        private String username;
        private String password;
        private String token;

        public ScoringEngineRegistrationResponse(int id, String url, String name, String username, String password, String token) {
            this.id = id;
            this.url = url;
            this.name = name;
            this.username = username;
            this.password = password;
            this.token = token;
        }

        public int getId() {
            return id;
        }

        public String getUrl() {
            return url;
        }

        public String getName() {
            return name;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getToken() {
            return token;
        }
    }
}
```

## 2. Middleware API Endpoints and Business Logic

### 2.1. Transaction Data API (`/transactions/{customerNumber}`)

#### Controller (`TransactionDataController.java`)

```java
package com.example.middleware.controller;

import com.example.middleware.service.TransactionDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class TransactionDataController {

    @Autowired
    private TransactionDataService transactionDataService;

    @GetMapping("/{customerNumber}")
    public ResponseEntity<?> getTransactionData(@PathVariable("customerNumber") String customerNumber) {
        try {
            String transactionData = transactionDataService.getTransactionData(customerNumber);
            return ResponseEntity.ok(transactionData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }

    static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
```

#### Service (`TransactionDataService.java`)

```java
package com.example.middleware.service;

import org.springframework.stereotype.Service;

@Service
public class TransactionDataService {

    public String getTransactionData(String customerNumber) {
        // 1. Call CBS to get transaction data (using Spring Integration)
        // 2. Transform data if needed
        // 3. Return transaction data
        return "[{\"accountNumber\":\"12345\",\"transactionDate\":\"2024-01-01\",\"amount\":100.0},{\"accountNumber\":\"12345\",\"transactionDate\":\"2024-01-02\",\"amount\":200.0}]"; // Placeholder
    }
}
```

## 3. Code Explanation and Notes

* **Controllers:** Handle HTTP requests, delegate to services.
* **Services:** Implement business logic, interact with repositories and external systems.
* **Repositories:** (LMS only) Interact with the database.
* **Feign Clients:** (LMS only) Provide a declarative way to make REST calls to external systems (Scoring Engine, CBS (in this example)).
* **DTOs:** Data Transfer Objects for request and response payloads.
* **Error Handling:** Basic error handling with ErrorResponse DTO. More robust exception handling should be implemented.
* **Placeholders:** Replace `YOUR_CBS_SOAP_ENDPOINT` and `YOUR_SCORING_ENGINE_ENDPOINT` with the actual URLs. Implement the actual SOAP calling logic.
* **SuperBase:** To use the superbase database instead, implement it in the logic.

## 4. Next Steps

1. Create the controller, service, repository, entity, client and config classes in your Spring Boot projects (LMS and Middleware).
2. Implement the business logic in the service classes.
3. Configure Spring Integration for SOAP communication (LMS and Middleware).
4. Configure Feign Clients for REST communication (LMS only).
5. Implement data persistence using JPA (LMS only).

## Key Improvements and Considerations

* **Code Examples:** I've provided complete code examples for the controllers and services.
* **Clear Separation of Concerns:** The code follows a clear separation of concerns (controller, service, repository).
* **Feign Clients:** Demonstrated how to define and use Feign clients for the Scoring Engine and CBS (although the CBS example uses a REST like implementation, you will need to modify to use Spring Integration for SOAP).
* **Data Transfer Objects (DTOs):** Used DTOs for request and response payloads.
* **Error Handling:** Included basic error handling. You should implement more robust exception handling in a real-world application.
* **Authentication/Authorization:** Authentication and authorization are not implemented in these code examples. You'll need to add them based on your chosen authentication mechanism (API Key, OAuth 2.0, etc.).
* **Service Annotation:** The `Service` annotation in the LMS application might need to be changed to point to the superbase service implementation.

## Your Tasks

1. **Create the Java classes and interfaces in your Spring Boot projects (LMS and Middleware).**
2. **Implement the business logic in the service classes, including the SOAP communication with the CBS (using Spring Integration).**
3. **Configure Spring Integration for SOAP communication.** This will involve defining message channels, endpoints, and transformers to handle the SOAP requests and responses.
4. **Configure Feign Clients for REST communication (LMS only).**
5. **Implement data persistence using JPA (LMS only).**

After you've completed these steps, we can move on to **Step 6: Database Design and Callback Platform (06_Database_Design.md).**
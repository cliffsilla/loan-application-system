# Database Design and Callback Platform

This document outlines the database schema for the Loan Management Module (LMS) and discusses the callback mechanism for the Scoring Engine.

## 1. Database Design (LMS)

The LMS requires a database to store customer information, loan applications, and related data. We'll use PostgreSQL for this purpose.

### 1.1. Entities and Tables

Here's a proposed database schema with entities and their corresponding tables:

#### Customer

* **Table Name**: `customers`
* **Columns**:
  * `customer_id` (UUID, Primary Key): Unique identifier for the customer.
  * `customer_number` (VARCHAR(255), Unique, Not Null): The customer's unique identifier in the bank's system.
  * `kyc_data` (JSONB): JSON data containing the Customer's KYC (Know Your Customer) information fetched from the CBS. Using JSONB allows for flexible storage of KYC attributes.
  * `created_at` (TIMESTAMP WITH TIME ZONE): Timestamp of when the customer record was created.
  * `updated_at` (TIMESTAMP WITH TIME ZONE): Timestamp of when the customer record was last updated.

#### Loan

* **Table Name**: `loans`
* **Columns**:
  * `loan_id` (UUID, Primary Key): Unique identifier for the loan application.
  * `customer_id` (UUID, Foreign Key referencing `customers.customer_id`, Not Null): The ID of the customer who applied for the loan.
  * `amount` (NUMERIC, Not Null): The requested loan amount.
  * `status` (VARCHAR(50), Not Null): The current status of the loan (e.g., PENDING, APPROVED, REJECTED, ACTIVE, COMPLETED).
  * `score` (NUMERIC): The credit score obtained from the Scoring Engine.
  * `limit` (NUMERIC): The approved loan limit based on the credit score.
  * `rejection_reason` (VARCHAR(255)): The reason for rejecting the loan application (if applicable).
  * `created_at` (TIMESTAMP WITH TIME ZONE): Timestamp of when the loan application was submitted.
  * `updated_at` (TIMESTAMP WITH TIME ZONE): Timestamp of when the loan application was last updated.

### 1.2. Relationships

* **One-to-Many**: A customer can have multiple loan applications. This is represented by the foreign key `customer_id` in the `loans` table referencing the `customer_id` in the `customers` table.

### 1.3. ERD (Entity Relationship Diagram) - Text Representation

```
customers {
  customer_id UUID PK
  customer_number VARCHAR(255) UN
  kyc_data JSONB
  created_at TIMESTAMP
  updated_at TIMESTAMP
}

loans {
  loan_id UUID PK
  customer_id UUID FK
  amount NUMERIC
  status VARCHAR(50)
  score NUMERIC
  limit NUMERIC
  rejection_reason VARCHAR(255)
  created_at TIMESTAMP
  updated_at TIMESTAMP
}
```

### 1.4. JPA Entities

Here's how the JPA entities would look:

#### `Customer.java`

```java
package com.example.lms.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "customer_number", unique = true, nullable = false)
    private String customerNumber;

    @Column(name = "kyc_data", columnDefinition = "jsonb")
    @Type(jakarta.json.JsonType.class)
    private String kycData;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Getters and setters...

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public String getKycData() {
        return kycData;
    }

    public void setKycData(String kycData) {
        this.kycData = kycData;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
```

#### `Loan.java`

```java
package com.example.lms.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "loan_id")
    private UUID loanId;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "score")
    private Double score;

    @Column(name = "limit")
    private Double limit;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Getters and setters...

    public UUID getLoanId() {
        return loanId;
    }

    public void setLoanId(UUID loanId) {
        this.loanId = loanId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getLimit() {
        return limit;
    }

    public void setLimit(Double limit) {
        this.limit = limit;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
```

### 1.5. JPA Repositories

#### `CustomerRepository.java`

```java
package com.example.lms.repository;

import com.example.lms.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByCustomerNumber(String customerNumber);
}
```

#### `LoanRepository.java`

```java
package com.example.lms.repository;

import com.example.lms.entity.Loan;
import com.example.lms.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {
    boolean existsByCustomerAndStatus(Customer customer, String status);
}
```

## 2. Callback Platform (Scoring Engine)

The Scoring Engine requires a callback URL to send transaction data to. The LMS needs to provide this endpoint.

### 2.1. Endpoint

The endpoint is already defined in 03_API_Endpoints.md:

`POST /scoring/callback`: Scoring Engine registers itself.

### 2.2. Implementation Considerations

* **Security**: The endpoint should be secured using Basic Authentication (as defined in 03_API_Endpoints.md).
* **Data Validation**: The LMS should validate the data received from the Scoring Engine.
* **Asynchronous Processing (Optional)**: For high-volume scenarios, consider using a message queue (e.g., RabbitMQ, Kafka) to process the callback requests asynchronously. This will prevent the LMS from being overwhelmed by the Scoring Engine.

## Key Improvements and Considerations

* **Database Schema**: A well-defined database schema with appropriate data types and relationships.
* **JSONB for KYC Data**: Using `JSONB` in PostgreSQL for flexible storage of KYC data. This is important because the KYC data structure might change over time.
* **JPA Entities**: Complete JPA entity definitions with annotations for mapping to the database tables.
* **JPA Repositories**: Interfaces for accessing and manipulating data in the database.
* **Callback Implementation**: Highlights the key considerations for implementing the callback endpoint.

## Your Tasks

1. **Create the database tables in your PostgreSQL database (or your chosen database).**
2. **Create the JPA entities and repositories in your LMS project.**
3. **Implement the callback endpoint (`/scoring/callback`) in your LMS controller and service.**
4. **Test the callback endpoint to ensure it correctly receives and processes data from the Scoring Engine.**

After you've completed these steps, we can move on to **Step 7: Testing Strategy (07_Testing.md).**
# Testing Strategy

This document outlines the testing strategy for the Loan Management Module (LMS) and the Middleware. We will use a combination of unit tests, integration tests, and potentially end-to-end tests.

## 1. Testing Levels

* **Unit Tests:** Test individual components (classes, methods) in isolation. Focus on verifying the logic within a single unit of code.
* **Integration Tests:** Test the interaction between different components or services. For example, testing the interaction between a controller and a service, or between the LMS and the Scoring Engine.
* **End-to-End (E2E) Tests (Optional, More Complex):** Test the entire application flow from the mobile app to the database and back. These tests simulate a real user interacting with the system. Due to the complexity of setting up a full E2E test environment with a mock mobile app and external services, this level of testing might be skipped or simplified for this exercise.

## 2. Testing Tools

* **JUnit:** A popular Java testing framework for writing unit and integration tests.
* **Mockito:** A mocking framework for creating mock objects to isolate units of code during testing.
* **Spring Test:** Provides utilities for testing Spring Boot applications, including mocking Spring beans and testing REST endpoints.
* **REST-assured:** A Java library for testing REST APIs.
* **WireMock:** A tool for creating mock SOAP and REST services.
* **Testcontainers:** A Java library that supports JUnit tests, providing lightweight, throwaway instances of common databases, Selenium web browsers, or anything else that can run in a Docker container.

## 3. Test Cases

Here's a breakdown of test cases for each component:

### 3.1. LMS

#### SubscriptionController

* **Unit Test:**
  * `subscribeCustomer_success()`: Tests successful customer subscription.
  * `subscribeCustomer_customerAlreadyExists()`: Tests the scenario where the customer is already subscribed.
  * `subscribeCustomer_invalidCustomerNumber()`: Tests with an invalid customer number.
* **Integration Test:**
  * `subscribeCustomer_integration()`: Tests the integration with the `CustomerService` and `CustomerRepository`.

#### LoanController

* **Unit Test:**
  * `requestLoan_success()`: Tests successful loan request.
  * `requestLoan_invalidAmount()`: Tests with an invalid loan amount.
* **Integration Test:**
  * `requestLoan_integration()`: Tests the integration with the `LoanService`, `CustomerService`, and `ScoringEngineClient`.

#### CustomerService

* **Unit Test:**
  * `subscribeCustomer_success()`: Tests successful customer subscription.
  * `subscribeCustomer_customerAlreadyExists()`: Tests the scenario where the customer is already subscribed.
* **Integration Test:**
  * Tests integration with `CBSClient` to retrieve KYC data (mock the CBS client).
  * Tests saving the customer to the database (mock the `CustomerRepository`).

#### LoanService

* **Unit Test:**
  * `requestLoan_success()`: Tests successful loan request.
  * `requestLoan_customerNotSubscribed()`: Tests the scenario where the customer is not subscribed.
  * `requestLoan_existingPendingLoan()`: Tests the scenario where the customer has an existing pending loan.
* **Integration Test:**
  * Tests integration with `ScoringEngineClient` to retrieve the credit score (mock the Scoring Engine client).
  * Tests saving the loan to the database (mock the `LoanRepository`).

#### ScoringCallbackController

* **Unit Test:**
  * `registerScoringEngine_success()`: Tests successful scoring engine registration.
  * `registerScoringEngine_invalidInput()`: Tests with invalid input parameters.
* **Integration Test:**
  * Test storing the callback URL (if you are storing this to the database)

#### CBSClient (Feign Client)

* **Integration Test:**
  * Tests communication with CBS (using WireMock to mock the CBS SOAP service). This is crucial.

### 3.2. Middleware

#### TransactionDataController

* **Unit Test:**
  * `getTransactionData_success()`: Tests successful retrieval of transaction data.
  * `getTransactionData_customerNotFound()`: Tests the scenario where the customer is not found.
* **Integration Test:**
  * `getTransactionData_integration()`: Tests the integration with the `TransactionDataService`.

#### TransactionDataService

* **Unit Test:**
  * `getTransactionData_success()`: Tests successful retrieval of transaction data.
* **Integration Test:**
  * Tests integration with CBS to retrieve transaction data (use WireMock to mock the CBS SOAP service).

## 4. Mocking Strategies

* Use Mockito to create mock objects for dependencies (e.g., `CustomerRepository`, `LoanRepository`, `ScoringEngineClient`, `CBSClient`).
* Use WireMock to create mock SOAP and REST services for the CBS and Scoring Engine. This allows you to test the integration with these external systems without actually calling them.
* Use Spring Test's `@MockBean` annotation to easily mock Spring beans in your test context.

## 5. Example Test Code (LMS - CustomerService Unit Test)

```java
package com.example.lms.service;

import com.example.lms.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceUnitTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    public void subscribeCustomer_success() {
        String customerNumber = "12345";
        when(customerRepository.findByCustomerNumber(customerNumber)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> customerService.subscribeCustomer(customerNumber));
    }

    @Test
    public void subscribeCustomer_customerAlreadyExists() {
        String customerNumber = "12345";
        // Mock the repository to return a customer when findByCustomerNumber is called
        when(customerRepository.findByCustomerNumber(customerNumber)).thenReturn(Optional.of(new Customer()));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            customerService.subscribeCustomer(customerNumber);
        });

        assertEquals("Customer already subscribed", exception.getMessage());
    }
}

## Notes

* This testing strategy provides a comprehensive approach to testing the LMS and Middleware.
* The specific test cases and implementation details may vary depending on your requirements and design choices.
* Remember to write tests before you write the actual code (Test-Driven Development - TDD) for better design and maintainability.

## Key Improvements and Considerations

* **Clear Testing Levels:** Defines Unit, Integration, and E2E tests.
* **Testing Tools:** Lists the commonly used testing tools in Spring Boot.
* **Detailed Test Cases:** Provides a detailed breakdown of test cases for each component.
* **Mocking Strategies:** Explains how to use Mockito and WireMock for mocking dependencies.
* **Example Test Code:** Includes a complete unit test example.

## Your Tasks

1. **Set up your testing environment in your Spring Boot projects (LMS and Middleware).**
2. **Write unit tests for each component (controller, service, repository).**
3. **Write integration tests to test the interactions between different components and services.**
4. **Use Mockito and WireMock to mock dependencies and external systems.**
5. **Run your tests and fix any bugs or issues that are identified.**

After you've completed these steps, we can move on to **Step 8: Deployment & Documentation (08_Deployment.md & 09_Documentation.md).**
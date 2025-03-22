# Business Logic
## Developing API Endpoints and Business Logic

This document outlines the steps to develop the API endpoints and business logic for the Loan Management Module (LMS) and the Middleware.

### 1. LMS API Endpoints and Business Logic

#### 1.1. Subscription API (`/subscriptions`)

##### Controller (`SubscriptionController.java`)

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
import javax.xml.bind.JAXBException;

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
        } catch (JAXBException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Error processing CBS response"));
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

##### Service (`CustomerService.java`)

```java
package com.example.lms.service;

import com.example.lms.entity.Customer;
import com.example.lms.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;
import java.time.OffsetDateTime;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MessageChannel cbsRequestChannel;

    public UUID subscribeCustomer(String customerNumber) throws JAXBException {
        // 1. Check if customer already exists
        if (customerRepository.findByCustomerNumber(customerNumber).isPresent()) {
            throw new RuntimeException("Customer already subscribed");
        }

        // 2. Call CBS to get KYC data (using Spring Integration)
        String soapRequest = generateCustomerRequest(customerNumber);
        Message<String> message = MessageBuilder.withPayload(soapRequest).build();
        cbsRequestChannel.send(message);

        //Assuming the response is handled by a separate service activator and transformer
        //For now, let's mock the response

        CustomerResponse kycData = parseCustomerResponse("<CustomerResponse xmlns=\"http://credable.io/cbs/customer\">\n" +
                "    <customer>\n" +
                "      <createdAt>2024-01-26T12:00:00+00:00</createdAt>\n" +
                "      <customerNumber>"+customerNumber+"</customerNumber>\n" +
                "      <firstName>John</firstName>\n" +
                "      <lastName>Doe</lastName>\n" +
                "      <monthlyIncome>5000.0</monthlyIncome>\n" +
                "    </customer>\n" +
                "  </CustomerResponse>");

        // 3. Store customer data (including KYC data)
        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setCustomerNumber(customerNumber);
        customer.setKycData(convertObjectToJSON(kycData)); // Store KYC data as JSON
        customer.setCreatedAt(OffsetDateTime.now());
        customer.setUpdatedAt(OffsetDateTime.now());
        customerRepository.save(customer);

        return customer.getCustomerId();
    }


    private String generateCustomerRequest(String customerNumber) throws JAXBException {
        CustomerRequest request = new CustomerRequest();
        request.setCustomerNumber(customerNumber);

        JAXBContext jaxbContext = JAXBContext.newInstance(CustomerRequest.class);
        Marshaller marshaller = jaxbContext.createMarshaller();

        StringWriter sw = new StringWriter();
        marshaller.marshal(request, sw);

        return sw.toString();
    }


    private CustomerResponse parseCustomerResponse(String xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(CustomerResponse.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        StringReader reader = new StringReader(xml);

        return (CustomerResponse) unmarshaller.unmarshal(reader);
    }

    private String convertObjectToJSON(CustomerResponse customerResponse) {
        //Implement your own logic
        return "{\"firstName\":\"John\",\"lastName\":\"Doe\"}";
    }

    //JAXB Classes

    @XmlRootElement(name = "CustomerRequest", namespace = "http://credable.io/cbs/customer")
    static class CustomerRequest {
        private String customerNumber;

        public String getCustomerNumber() {
            return customerNumber;
        }

        @XmlElement(namespace = "http://credable.io/cbs/customer")
        public void setCustomerNumber(String customerNumber) {
            this.customerNumber = customerNumber;
        }
    }

    @XmlRootElement(name = "CustomerResponse", namespace = "http://credable.io/cbs/customer")
    static class CustomerResponse {
        private Customer customer;

        public Customer getCustomer() {
            return customer;
        }

        @XmlElement(namespace = "http://credable.io/cbs/customer")
        public void setCustomer(Customer customer) {
            this.customer = customer;
        }

        static class Customer {
            private String customerNumber;
            private String firstName;
            private String lastName;
            private double monthlyIncome;
            private String createdAt;

            public String getCustomerNumber() {
                return customerNumber;
            }

            @XmlElement(namespace = "http://credable.io/cbs/customer")
            public void setCustomerNumber(String customerNumber) {
                this.customerNumber = customerNumber;
            }

            public String getFirstName() {
                return firstName;
            }

            @XmlElement(namespace = "http://credable.io/cbs/customer")
            public void setFirstName(String firstName) {
                this.firstName = firstName;
            }

            public String getLastName() {
                return lastName;
            }

            @XmlElement(namespace = "http://credable.io/cbs/customer")
            public void setLastName(String lastName) {
                this.lastName = lastName;
            }

            public double getMonthlyIncome() {
                return monthlyIncome;
            }

            @XmlElement(namespace = "http://credable.io/cbs/customer")
            public void setMonthlyIncome(double monthlyIncome) {
                this.monthlyIncome = monthlyIncome;
            }

            public String getCreatedAt() {
                return createdAt;
            }

            @XmlElement(namespace = "http://credable.io/cbs/customer")
            public void setCreatedAt(String createdAt) {
                this.createdAt = createdAt;
            }
        }
    }
}
```

#### 1.2. Integration Configuration

##### CBS Integration Configuration (`CBSConfig.java`)

```java
@Configuration
@EnableIntegration
public class CBSConfig {

    @Bean
    public MessageChannel cbsRequestChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel cbsResponseChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "cbsRequestChannel")
    public void callCbs(Message<String> request) {
        WebServiceTemplate webServiceTemplate = webServiceTemplate();
        webServiceTemplate.sendSourceAndReceiveToResult(new StringSource(request.getPayload()), new DOMResult());
    }

    @Bean
    public WebServiceTemplate webServiceTemplate() {
        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        webServiceTemplate.setDefaultUri("http://kycapitest.credable.io/service/customerWsdl.wsdl"); // Replace with the actual WSDL URL
        return webServiceTemplate;
    }
}
```

### 2. Middleware API Endpoints and Business Logic

#### 2.1. Transaction Data API (`/transactions/{customerNumber}`)

##### Controller (`TransactionDataController.java`)

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

import javax.xml.bind.JAXBException;

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
        }  catch (JAXBException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Error processing CBS response"));
        }catch (Exception e) {
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

##### Service (`TransactionDataService.java`)

```java
package com.example.middleware.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.StringReader;
import java.io.StringWriter;

@Service
public class TransactionDataService {

    @Autowired
    private MessageChannel cbsRequestChannel;

    public String getTransactionData(String customerNumber) throws JAXBException {

        String soapRequest = generateTransactionRequest(customerNumber);
        Message<String> message = MessageBuilder.withPayload(soapRequest).build();
        cbsRequestChannel.send(message);

        //Assume the response is handled by a separate service activator and transformer
        //For now, let's mock the response
        String soapResponse = "<TransactionsResponse xmlns=\"http://credable.io/cbs/transaction\">\n" +
                "    <transactions>\n" +
                "      <accountNumber>12345</accountNumber>\n" +
                "      <transactionDate>2024-01-01</transactionDate>\n" +
                "      <amount>100.0</amount>\n" +
                "    </transactions>\n" +
                "  </TransactionsResponse>";

        TransactionsResponse transactionsResponse = parseTransactionsResponse(soapResponse);


        //Transform the data as per scoring engine
        String transactionData = convertObjectToJSON(transactionsResponse);

        return transactionData;
    }

    private String generateTransactionRequest(String customerNumber) throws JAXBException {
        TransactionsRequest request = new TransactionsRequest();
        request.setCustomerNumber(customerNumber);

        JAXBContext jaxbContext = JAXBContext.newInstance(TransactionsRequest.class);
        Marshaller marshaller = jaxbContext.createMarshaller();

        StringWriter sw = new StringWriter();
        marshaller.marshal(request, sw);

        return sw.toString();
    }

    private TransactionsResponse parseTransactionsResponse(String xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(TransactionsResponse.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        StringReader reader = new StringReader(xml);

        return (TransactionsResponse) unmarshaller.unmarshal(reader);
    }

    private String convertObjectToJSON(TransactionsResponse transactionsResponse) {
        //Implement your own logic
        return "[{\"accountNumber\":\"12345\",\"transactionDate\":\"2024-01-01\",\"amount\":100.0}]";
    }

    //JAXB Classes
    @XmlRootElement(name = "TransactionsRequest", namespace = "http://credable.io/cbs/transaction")
    static class TransactionsRequest {
        private String customerNumber;

        public String getCustomerNumber() {
            return customerNumber;
        }

        @XmlElement(namespace = "http://credable.io/cbs/transaction")
        public void setCustomerNumber(String customerNumber) {
            this.customerNumber = customerNumber;
        }
    }

    @XmlRootElement(name = "TransactionsResponse", namespace = "http://credable.io/cbs/transaction")
    static class TransactionsResponse {
        private Transaction transaction;

        public Transaction getTransaction() {
            return transaction;
        }

        @XmlElement(namespace = "http://credable.io/cbs/transaction")
        public void setTransaction(Transaction transaction) {
            this.transaction = transaction;
        }

        static class Transaction {
            private String accountNumber;
            private String transactionDate;
            private double amount;

            public String getAccountNumber() {
                return accountNumber;
            }

            @XmlElement(namespace = "http://credable.io/cbs/transaction")
            public void setAccountNumber(String accountNumber) {
                this.accountNumber = accountNumber;
            }

            public String getTransactionDate() {
                return transactionDate;
            }

            @XmlElement(namespace = "http://credable.io/cbs/transaction")
            public void setTransactionDate(String transactionDate) {
                this.transactionDate = transactionDate;
            }

            public double getAmount() {
                return amount;
            }

            @XmlElement(namespace = "http://credable.io/cbs/transaction")
            public void setAmount(double amount) {
                this.amount = amount;
            }
        }
    }
}
```

### 3. Code Explanation and Notes

- **Controllers**: Handle HTTP requests, delegate to services.
- **Services**: Implement business logic, interact with repositories and external systems.
- **Repositories**: (LMS only) Interact with the database.
- **JAXB**: JAXB (Java Architecture for XML Binding) is used for marshalling (converting Java objects to XML) and unmarshalling (converting XML to Java objects) the SOAP requests and responses. You will need to create JAXB classes that match the structure of the WSDL.
- **Spring Integration**: Used for handling SOAP calls to CBS.
- **DTOs**: Data Transfer Objects for request and response payloads.
- **Error Handling**: Basic error handling with ErrorResponse DTO. More robust exception handling should be implemented.
- **Placeholders**: Replace YOUR_CBS_SOAP_ENDPOINT and YOUR_SCORING_ENGINE_ENDPOINT with the actual URLs. Implement the actual SOAP calling logic.

### 4. Next Steps

1. Create the controller and service classes in your Spring Boot projects (LMS and Middleware).
2. Implement the business logic in the service classes, including the SOAP communication with the CBS (using Spring Integration).
3. Configure Spring Integration for SOAP communication. This will involve defining message channels, endpoints, and transformers to handle the SOAP requests and responses.
4. Create JAXB classes from WSDL.
5. Implement data persistence using JPA (LMS only).

### 5. Important Considerations

- Implement the logic to read data from the SOAP Responses.
- For clarity, a StringSource is used for the message transformation. Use the right implementation as per your use case.
- To generate JAXB classes from the WSDL, you can use a tool like xjc (part of the JDK). Example: `xjc -d src/main/java -p com.example.lms.cbs customerWsdl.wsdl`. This will generate Java classes in the com.example.lms.cbs package based on the customerWsdl.wsdl file.

This document provides a comprehensive guide for implementing the API endpoints and business logic for the LMS and Middleware, including SOAP integration with Spring Integration.
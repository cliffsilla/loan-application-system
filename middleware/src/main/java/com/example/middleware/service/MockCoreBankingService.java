package com.example.middleware.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Mock implementation of the Core Banking System service that simulates banking operations
 * when the actual service is unavailable.
 * This service is only active when the "mock" profile is enabled.
 */
@Service
@Profile("mock")
@Primary
public class MockCoreBankingService {

    private static final Logger logger = LoggerFactory.getLogger(MockCoreBankingService.class);
    private static final Random random = new Random();
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // In-memory storage for customer accounts
    private final Map<String, CustomerAccount> customerAccounts = new HashMap<>();
    
    public MockCoreBankingService() {
        logger.info("Initialized MockCoreBankingService - using simulated core banking responses");
    }
    
    /**
     * Check if a customer exists in the system
     * @param customerNumber The customer number to check
     * @return true if the customer exists, false otherwise
     */
    public boolean customerExists(String customerNumber) {
        // Simulate processing delay
        simulateDelay(200);
        
        // If customer doesn't exist in our map, create with 50% probability
        if (!customerAccounts.containsKey(customerNumber)) {
            if (random.nextBoolean()) {
                createCustomerAccount(customerNumber);
                logger.info("Created new mock customer account for {}", customerNumber);
                return true;
            }
            logger.info("Customer {} does not exist in mock core banking system", customerNumber);
            return false;
        }
        
        logger.info("Customer {} exists in mock core banking system", customerNumber);
        return true;
    }
    
    /**
     * Get customer account details
     * @param customerNumber The customer number
     * @return A map containing customer account details or null if customer doesn't exist
     */
    public Map<String, Object> getCustomerAccount(String customerNumber) {
        // Simulate processing delay
        simulateDelay(300);
        
        if (!customerAccounts.containsKey(customerNumber)) {
            if (customerExists(customerNumber)) {
                // Customer was just created by the customerExists method
                return convertToMap(customerAccounts.get(customerNumber));
            }
            logger.warn("Attempted to get account details for non-existent customer {}", customerNumber);
            return null;
        }
        
        CustomerAccount account = customerAccounts.get(customerNumber);
        logger.info("Retrieved mock account details for customer {}", customerNumber);
        return convertToMap(account);
    }
    
    /**
     * Create a loan for a customer
     * @param customerNumber The customer number
     * @param amount The loan amount
     * @return A map containing the loan details or null if the operation failed
     */
    public Map<String, Object> createLoan(String customerNumber, double amount) {
        // Simulate processing delay
        simulateDelay(500);
        
        if (!customerAccounts.containsKey(customerNumber)) {
            if (!customerExists(customerNumber)) {
                logger.warn("Cannot create loan for non-existent customer {}", customerNumber);
                return null;
            }
        }
        
        CustomerAccount account = customerAccounts.get(customerNumber);
        
        // 80% success rate for loan creation
        if (random.nextDouble() < 0.8) {
            String loanId = "LOAN" + (10000 + random.nextInt(90000));
            Loan loan = new Loan(loanId, amount, "PENDING", LocalDate.now());
            account.loans.add(loan);
            
            logger.info("Created mock loan {} of amount {} for customer {}", loanId, amount, customerNumber);
            return convertToMap(loan);
        } else {
            logger.warn("Failed to create mock loan for customer {}", customerNumber);
            return null;
        }
    }
    
    /**
     * Update a loan status
     * @param customerNumber The customer number
     * @param loanId The loan ID
     * @param status The new status
     * @return true if the update was successful, false otherwise
     */
    public boolean updateLoanStatus(String customerNumber, String loanId, String status) {
        // Simulate processing delay
        simulateDelay(300);
        
        if (!customerAccounts.containsKey(customerNumber)) {
            logger.warn("Cannot update loan for non-existent customer {}", customerNumber);
            return false;
        }
        
        CustomerAccount account = customerAccounts.get(customerNumber);
        Optional<Loan> loanOpt = account.loans.stream()
                .filter(loan -> loan.loanId.equals(loanId))
                .findFirst();
        
        if (loanOpt.isPresent()) {
            Loan loan = loanOpt.get();
            loan.status = status;
            logger.info("Updated mock loan {} status to {} for customer {}", loanId, status, customerNumber);
            return true;
        } else {
            logger.warn("Loan {} not found for customer {}", loanId, customerNumber);
            return false;
        }
    }
    
    /**
     * Get all loans for a customer
     * @param customerNumber The customer number
     * @return A list of maps containing loan details or an empty list if no loans exist
     */
    public List<Map<String, Object>> getCustomerLoans(String customerNumber) {
        // Simulate processing delay
        simulateDelay(400);
        
        if (!customerAccounts.containsKey(customerNumber)) {
            logger.warn("Cannot get loans for non-existent customer {}", customerNumber);
            return Collections.emptyList();
        }
        
        CustomerAccount account = customerAccounts.get(customerNumber);
        List<Map<String, Object>> loans = new ArrayList<>();
        
        for (Loan loan : account.loans) {
            loans.add(convertToMap(loan));
        }
        
        logger.info("Retrieved {} mock loans for customer {}", loans.size(), customerNumber);
        return loans;
    }
    
    /**
     * Get a specific loan by ID
     * @param loanId The loan ID
     * @return A map containing the loan details or null if not found
     */
    public Map<String, Object> getLoanById(String loanId) {
        // Simulate processing delay
        simulateDelay(300);
        
        for (CustomerAccount account : customerAccounts.values()) {
            Optional<Loan> loanOpt = account.loans.stream()
                    .filter(loan -> loan.loanId.equals(loanId))
                    .findFirst();
            
            if (loanOpt.isPresent()) {
                Loan loan = loanOpt.get();
                logger.info("Retrieved mock loan {} for customer {}", loanId, account.customerNumber);
                return convertToMap(loan);
            }
        }
        
        logger.warn("Loan {} not found in mock core banking system", loanId);
        return null;
    }
    
    /**
     * Test the connection to the core banking system
     * @return true (mock service is always connected)
     */
    public boolean testConnection() {
        logger.info("Testing connection to mock core banking system");
        return true; // Mock service is always connected
    }
    
    // Helper methods
    
    private void createCustomerAccount(String customerNumber) {
        CustomerAccount account = new CustomerAccount();
        account.customerNumber = customerNumber;
        account.name = "Mock Customer " + customerNumber;
        account.email = "customer" + customerNumber + "@example.com";
        account.phoneNumber = "+" + (1000000000 + random.nextInt(9000000));
        account.status = "ACTIVE";
        account.createdDate = LocalDate.now().minusDays(random.nextInt(365));
        account.loans = new ArrayList<>();
        
        customerAccounts.put(customerNumber, account);
    }
    
    private void simulateDelay(int maxMillis) {
        try {
            Thread.sleep(random.nextInt(maxMillis));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private Map<String, Object> convertToMap(CustomerAccount account) {
        Map<String, Object> map = new HashMap<>();
        map.put("customerNumber", account.customerNumber);
        map.put("name", account.name);
        map.put("email", account.email);
        map.put("phoneNumber", account.phoneNumber);
        map.put("status", account.status);
        map.put("createdDate", account.createdDate.format(dateFormatter));
        return map;
    }
    
    private Map<String, Object> convertToMap(Loan loan) {
        Map<String, Object> map = new HashMap<>();
        map.put("loanId", loan.loanId);
        map.put("amount", loan.amount);
        map.put("status", loan.status);
        map.put("createdDate", loan.createdDate.format(dateFormatter));
        return map;
    }
    
    // Inner classes for data models
    
    private static class CustomerAccount {
        String customerNumber;
        String name;
        String email;
        String phoneNumber;
        String status;
        LocalDate createdDate;
        List<Loan> loans;
    }
    
    private static class Loan {
        String loanId;
        double amount;
        String status;
        LocalDate createdDate;
        
        Loan(String loanId, double amount, String status, LocalDate createdDate) {
            this.loanId = loanId;
            this.amount = amount;
            this.status = status;
            this.createdDate = createdDate;
        }
    }
}

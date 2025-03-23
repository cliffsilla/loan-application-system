package com.example.middleware.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Mock implementation of scoring engine service that simulates the scoring engine
 * when the actual service is unavailable.
 * This service is only active when the "mock" profile is enabled.
 */
@Service
@Profile("mock")
@Primary
public class MockScoringEngineService extends ScoringEngineService {

    private static final Logger logger = LoggerFactory.getLogger(MockScoringEngineService.class);
    private static final Random random = new Random();
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public MockScoringEngineService(RestTemplate restTemplate) {
        super(restTemplate);
        logger.info("Initialized MockScoringEngineService - using simulated scoring engine responses");
    }
    
    /**
     * Retrieves a customer score from the Scoring Engine
     * @param customerNumber The customer number
     * @return The score response
     */
    public Map<String, Object> getCustomerScore(String customerNumber) {
        logger.info("Simulating scoring engine response for customer {}", customerNumber);
        
        // Simulate processing delay
        try {
            Thread.sleep(500); // Simulate a 500ms processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("customerNumber", customerNumber);
        
        // Generate a score between 300 and 850 (typical credit score range)
        int score = 300 + random.nextInt(551);
        response.put("score", score);
        
        // Set credit limit based on score
        double limit = calculateCreditLimit(score);
        response.put("limit", limit);
        
        // Add timestamp
        response.put("timestamp", LocalDate.now().format(dateFormatter));
        
        logger.info("Generated mock score {} and limit {} for customer {}", score, limit, customerNumber);
        return response;
    }
    
    /**
     * Retrieves customer transactions
     * @param customerNumber The customer number
     * @return List of transactions
     */
    public List<Map<String, Object>> getCustomerTransactions(String customerNumber) {
        logger.info("Simulating transaction data for customer {}", customerNumber);
        
        // Simulate processing delay
        try {
            Thread.sleep(800); // Simulate a 800ms processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        List<Map<String, Object>> transactions = new ArrayList<>();
        
        // Generate between 5 and 15 transactions
        int transactionCount = 5 + random.nextInt(11);
        
        LocalDate currentDate = LocalDate.now();
        
        for (int i = 0; i < transactionCount; i++) {
            Map<String, Object> transaction = new HashMap<>();
            transaction.put("transactionId", UUID.randomUUID().toString());
            transaction.put("customerNumber", customerNumber);
            transaction.put("accountNumber", "ACC" + (10000 + random.nextInt(90000)));
            
            // Random amount between 10 and 1000
            double amount = 10 + (random.nextDouble() * 990);
            transaction.put("amount", Math.round(amount * 100.0) / 100.0); // Round to 2 decimal places
            
            // Random date within the last 30 days
            LocalDate transactionDate = currentDate.minusDays(random.nextInt(30));
            transaction.put("transactionDate", transactionDate.format(dateFormatter));
            
            // Random transaction type
            String[] types = {"DEBIT", "CREDIT", "TRANSFER", "PAYMENT", "WITHDRAWAL"};
            transaction.put("type", types[random.nextInt(types.length)]);
            
            transactions.add(transaction);
        }
        
        logger.info("Generated {} mock transactions for customer {}", transactionCount, customerNumber);
        return transactions;
    }
    
    /**
     * Registers a client with the Scoring Engine
     * @param customerNumber The customer number
     * @param name The customer name
     * @param email The customer email
     * @return true if registration was successful, false otherwise
     */
    public boolean registerClient(String customerNumber, String name, String email) {
        logger.info("Simulating client registration for customer {}", customerNumber);
        
        // Simulate processing delay
        try {
            Thread.sleep(300); // Simulate a 300ms processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 90% success rate for registrations
        boolean success = random.nextDouble() < 0.9;
        
        if (success) {
            logger.info("Successfully registered client {} in mock scoring engine", customerNumber);
        } else {
            logger.warn("Failed to register client {} in mock scoring engine", customerNumber);
        }
        
        return success;
    }
    
    /**
     * Registers a client with the Scoring Engine
     * @param clientData The client registration data
     * @return The registration response
     */
    public Map<String, Object> registerClient(Map<String, Object> clientData) {
        String customerNumber = clientData.getOrDefault("customerNumber", "unknown").toString();
        String name = clientData.getOrDefault("name", "Unknown Name").toString();
        String email = clientData.getOrDefault("email", "unknown@example.com").toString();
        
        boolean success = registerClient(customerNumber, name, email);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("customerNumber", customerNumber);
        
        if (success) {
            response.put("message", "Client registered successfully");
            response.put("registrationDate", LocalDate.now().format(dateFormatter));
        } else {
            response.put("message", "Failed to register client");
            response.put("error", "Mock registration failure");
        }
        
        return response;
    }
    
    /**
     * Registers a client with the Scoring Engine using the createClient endpoint
     * @param clientData The client registration data
     * @return The registration response
     */
    public Map<String, Object> createClient(Map<String, Object> clientData) {
        return registerClient(clientData); // Same implementation for mock service
    }
    
    /**
     * Test the connection to the scoring engine
     * @return true if the connection is successful, false otherwise
     */
    @Override
    public boolean testConnection() {
        logger.info("Testing connection to mock scoring engine");
        return true; // Mock service is always connected
    }
    
    /**
     * Get the scoring engine URL
     * @return the scoring engine URL
     */
    @Override
    public String getScoringEngineUrl() {
        return "mock://scoring-engine";
    }
    
    private double calculateCreditLimit(int score) {
        if (score < 500) {
            return 0; // No credit for very low scores
        } else if (score < 600) {
            return 500 + (score - 500) * 5; // 500-1000
        } else if (score < 700) {
            return 1000 + (score - 600) * 10; // 1000-2000
        } else if (score < 800) {
            return 2000 + (score - 700) * 30; // 2000-5000
        } else {
            return 5000 + (score - 800) * 100; // 5000-10000
        }
    }
}

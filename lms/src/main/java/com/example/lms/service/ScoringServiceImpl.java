package com.example.lms.service;

import com.example.lms.client.ScoringEngineClient;
import com.example.lms.entity.Loan;
import com.example.lms.entity.LoanStatus;
import com.example.lms.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ScoringServiceImpl implements ScoringService {

    private final CustomerService customerService;
    private final LoanService loanService;
    private final LoanRepository loanRepository;
    private final ScoringEngineClient scoringEngineClient;
    
    // In-memory store to track scoring requests by token
    // In a production environment, this should be stored in a database
    private final Map<String, String> tokenToCustomerMap = new ConcurrentHashMap<>();

    @Override
    public String initiateScoreQuery(String customerNumber) {
        // Validate customer exists
        customerService.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // Generate a unique token
        String token = UUID.randomUUID().toString();
        
        // Store the mapping of token to customer number
        tokenToCustomerMap.put(token, customerNumber);
        
        // Call the scoring engine to initiate scoring
        Map<String, Object> request = new HashMap<>();
        request.put("customerNumber", customerNumber);
        request.put("token", token);
        
        try {
            scoringEngineClient.getScore(request);
            return token;
        } catch (Exception e) {
            tokenToCustomerMap.remove(token);
            throw new RuntimeException("Failed to initiate score query: " + e.getMessage(), e);
        }
    }

    @Override
    public Loan processScoreCallback(String token, Double score, Double limit) {
        // Validate token exists
        if (!tokenToCustomerMap.containsKey(token)) {
            throw new RuntimeException("Invalid token");
        }
        
        // Get the customer number from the token
        String customerNumber = tokenToCustomerMap.get(token);
        
        // Find the customer's pending loan
        Loan pendingLoan = loanRepository.findByCustomer(customerService.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found"))).stream()
                .filter(loan -> LoanStatus.PENDING.name().equals(loan.getStatus()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No pending loan found for customer"));
        
        // Update the loan with score and limit
        loanService.updateLoanWithScoreAndLimit(pendingLoan, score, limit);
        
        // Clean up the token mapping
        tokenToCustomerMap.remove(token);
        
        return pendingLoan;
    }
    
    @Override
    public void scoreLoan(Loan loan) {
        // This method initiates the scoring process for a loan
        // In a real implementation, this would call the Scoring Engine asynchronously
        // For now, we'll use mock values for demonstration purposes
        
        try {
            // In a real implementation, we would call initiateScoreQuery with the customer number
            // and wait for a callback from the Scoring Engine
            // For demonstration, we'll directly update the loan with mock values
            double mockScore = 750.0; // Mock score
            double mockLimit = 10000.0; // Mock limit
            
            // Update the loan with the mock score and limit
            loanService.updateLoanWithScoreAndLimit(loan, mockScore, mockLimit);
        } catch (Exception e) {
            throw new RuntimeException("Failed to score loan: " + e.getMessage(), e);
        }
    }
}

package com.example.lms.service;

import com.example.lms.entity.Loan;

import java.util.UUID;

public interface ScoringService {
    /**
     * Initiates a score query for a customer
     * @param customerNumber The customer number
     * @return A token to track the score query
     */
    String initiateScoreQuery(String customerNumber);
    
    /**
     * Processes a scoring callback from the Scoring Engine
     * @param token The token from the initial score query
     * @param score The calculated score
     * @param limit The calculated limit
     * @return The updated loan
     */
    Loan processScoreCallback(String token, Double score, Double limit);
    
    /**
     * Initiates the scoring process for a loan
     * @param loan The loan to score
     */
    void scoreLoan(Loan loan);
}

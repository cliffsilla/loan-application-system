package com.example.lms.service;

import com.example.lms.entity.Loan;

import java.util.Optional;
import java.util.UUID;

public interface LoanService {
    Loan createLoanApplication(String customerNumber, Double amount);
    Optional<Loan> getLoanStatus(UUID loanId);
    // This method would normally call the Scoring Engine to get score and limit
    void updateLoanWithScoreAndLimit(Loan loan, Double score, Double limit);
}

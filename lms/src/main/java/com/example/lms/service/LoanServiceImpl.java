package com.example.lms.service;

import com.example.lms.entity.Customer;
import com.example.lms.entity.Loan;
import com.example.lms.entity.LoanStatus;
import com.example.lms.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final CustomerService customerService;

    @Override
    public Loan createLoanApplication(String customerNumber, Double amount) {
        // Find customer or throw exception
        Customer customer = customerService.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new RuntimeException("Customer not subscribed"));

        // Check if customer has existing loan applications in progress
        List<String> activeStatuses = Arrays.asList(
                LoanStatus.PENDING.name(),
                LoanStatus.APPROVED.name(),
                LoanStatus.ACTIVE.name()
        );

        if (loanRepository.existsByCustomerAndStatusIn(customer, activeStatuses)) {
            throw new RuntimeException("Customer has existing loan");
        }

        // Create new loan application
        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setAmount(amount);
        loan.setStatus(LoanStatus.PENDING.name());

        // Save and return
        return loanRepository.save(loan);
    }

    @Override
    public Optional<Loan> getLoanStatus(UUID loanId) {
        return loanRepository.findByLoanId(loanId);
    }

    @Override
    public void updateLoanWithScoreAndLimit(Loan loan, Double score, Double limit) {
        loan.setScore(score);
        loan.setLimit(limit);

        // Determine if loan is approved or rejected based on score and limit
        if (score >= 700 && limit >= loan.getAmount()) {
            loan.setStatus(LoanStatus.APPROVED.name());
        } else {
            loan.setStatus(LoanStatus.REJECTED.name());
            if (score < 700) {
                loan.setRejectionReason("Credit score too low");
            } else {
                loan.setRejectionReason("Requested amount exceeds approved limit");
            }
        }

        loanRepository.save(loan);
    }
}

package com.example.lms.repository;

import com.example.lms.entity.Loan;
import com.example.lms.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {
    boolean existsByCustomerAndStatusIn(Customer customer, List<String> statuses);
    List<Loan> findByCustomer(Customer customer);
    Optional<Loan> findByLoanId(UUID loanId);
}

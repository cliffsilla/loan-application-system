package com.example.lms.service;

import com.example.lms.entity.Customer;
import com.example.lms.entity.Loan;
import com.example.lms.entity.LoanStatus;
import com.example.lms.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class LoanServiceIntegrationTest {

    @Autowired
    private LoanService loanService;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private LoanRepository loanRepository;

    @Test
    public void createLoanApplication_integration() {
        // Arrange
        String customerNumber = "12345";
        Double amount = 5000.0;
        
        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setCustomerNumber(customerNumber);
        
        Loan savedLoan = new Loan();
        savedLoan.setLoanId(UUID.randomUUID());
        savedLoan.setCustomer(customer);
        savedLoan.setAmount(amount);
        savedLoan.setStatus(LoanStatus.PENDING.name());
        
        when(customerService.findByCustomerNumber(customerNumber)).thenReturn(Optional.of(customer));
        when(loanRepository.existsByCustomerAndStatusIn(eq(customer), any())).thenReturn(false);
        when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);

        // Act
        Loan result = loanService.createLoanApplication(customerNumber, amount);

        // Assert
        assertNotNull(result);
        assertEquals(amount, result.getAmount());
        assertEquals(LoanStatus.PENDING.name(), result.getStatus());
        assertEquals(customer, result.getCustomer());
        
        verify(customerService, times(1)).findByCustomerNumber(customerNumber);
        verify(loanRepository, times(1)).existsByCustomerAndStatusIn(eq(customer), any());
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    public void getLoanStatus_integration() {
        // Arrange
        UUID loanId = UUID.randomUUID();
        
        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setCustomerNumber("12345");
        
        Loan loan = new Loan();
        loan.setLoanId(loanId);
        loan.setCustomer(customer);
        loan.setAmount(5000.0);
        loan.setStatus(LoanStatus.APPROVED.name());
        
        when(loanRepository.findByLoanId(loanId)).thenReturn(Optional.of(loan));

        // Act
        Optional<Loan> result = loanService.getLoanStatus(loanId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(loanId, result.get().getLoanId());
        assertEquals(LoanStatus.APPROVED.name(), result.get().getStatus());
        verify(loanRepository, times(1)).findByLoanId(loanId);
    }

    @Test
    public void updateLoanWithScoreAndLimit_integration() {
        // Arrange
        Loan loan = new Loan();
        loan.setLoanId(UUID.randomUUID());
        loan.setAmount(5000.0);
        loan.setStatus(LoanStatus.PENDING.name());
        
        Double score = 750.0;
        Double limit = 10000.0;
        
        when(loanRepository.save(loan)).thenReturn(loan);

        // Act
        loanService.updateLoanWithScoreAndLimit(loan, score, limit);

        // Assert
        assertEquals(score, loan.getScore());
        assertEquals(limit, loan.getLimit());
        assertEquals(LoanStatus.APPROVED.name(), loan.getStatus());
        verify(loanRepository, times(1)).save(loan);
    }
}

package com.example.lms.service;

import com.example.lms.client.ScoringEngineClient;
import com.example.lms.entity.Customer;
import com.example.lms.entity.Loan;
import com.example.lms.entity.LoanStatus;
import com.example.lms.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class ScoringServiceIntegrationTest {

    @Autowired
    private ScoringService scoringService;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private LoanService loanService;

    @MockBean
    private LoanRepository loanRepository;

    @MockBean
    private ScoringEngineClient scoringEngineClient;

    @Test
    public void initiateScoreQuery_integration() {
        // Arrange
        String customerNumber = "12345";
        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setCustomerNumber(customerNumber);
        
        when(customerService.findByCustomerNumber(customerNumber)).thenReturn(Optional.of(customer));
        doNothing().when(scoringEngineClient).getScore(any(Map.class));

        // Act
        String token = scoringService.initiateScoreQuery(customerNumber);

        // Assert
        assertNotNull(token);
        verify(customerService, times(1)).findByCustomerNumber(customerNumber);
        verify(scoringEngineClient, times(1)).getScore(any(Map.class));
    }

    @Test
    public void scoreLoan_integration() {
        // Arrange
        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setCustomerNumber("12345");
        
        Loan loan = new Loan();
        loan.setLoanId(UUID.randomUUID());
        loan.setCustomer(customer);
        loan.setAmount(5000.0);
        loan.setStatus(LoanStatus.PENDING.name());
        
        doNothing().when(loanService).updateLoanWithScoreAndLimit(any(Loan.class), anyDouble(), anyDouble());

        // Act
        scoringService.scoreLoan(loan);

        // Assert
        verify(loanService, times(1)).updateLoanWithScoreAndLimit(eq(loan), anyDouble(), anyDouble());
    }

    // Note: processScoreCallback is difficult to test in integration due to the private token map
    // In a real application, you would store tokens in a database or other persistent store
}

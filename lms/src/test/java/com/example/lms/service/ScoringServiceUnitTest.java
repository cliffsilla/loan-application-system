package com.example.lms.service;

import com.example.lms.client.ScoringEngineClient;
import com.example.lms.entity.Customer;
import com.example.lms.entity.Loan;
import com.example.lms.entity.LoanStatus;
import com.example.lms.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScoringServiceUnitTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private LoanService loanService;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private ScoringEngineClient scoringEngineClient;

    @InjectMocks
    private ScoringServiceImpl scoringService;

    @Test
    public void initiateScoreQuery_success() {
        // Arrange
        String customerNumber = "12345";
        Customer customer = new Customer();
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
    public void initiateScoreQuery_customerNotFound() {
        // Arrange
        String customerNumber = "12345";
        when(customerService.findByCustomerNumber(customerNumber)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            scoringService.initiateScoreQuery(customerNumber);
        });
        
        assertEquals("Customer not found", exception.getMessage());
        verify(customerService, times(1)).findByCustomerNumber(customerNumber);
        verify(scoringEngineClient, never()).getScore(any(Map.class));
    }

    @Test
    public void initiateScoreQuery_scoringEngineFailed() {
        // Arrange
        String customerNumber = "12345";
        Customer customer = new Customer();
        customer.setCustomerNumber(customerNumber);
        
        when(customerService.findByCustomerNumber(customerNumber)).thenReturn(Optional.of(customer));
        doThrow(new RuntimeException("Scoring engine error")).when(scoringEngineClient).getScore(any(Map.class));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            scoringService.initiateScoreQuery(customerNumber);
        });
        
        assertTrue(exception.getMessage().contains("Failed to initiate score query"));
        verify(customerService, times(1)).findByCustomerNumber(customerNumber);
        verify(scoringEngineClient, times(1)).getScore(any(Map.class));
    }

    @Test
    public void processScoreCallback_success() throws Exception {
        // Arrange
        String token = UUID.randomUUID().toString();
        String customerNumber = "12345";
        Double score = 750.0;
        Double limit = 10000.0;
        
        Customer customer = new Customer();
        customer.setCustomerNumber(customerNumber);
        
        Loan pendingLoan = new Loan();
        pendingLoan.setCustomer(customer);
        pendingLoan.setStatus(LoanStatus.PENDING.name());
        
        // Use reflection to set the token in the private map
        java.lang.reflect.Field field = ScoringServiceImpl.class.getDeclaredField("tokenToCustomerMap");
        field.setAccessible(true);
        Map<String, String> tokenMap = (Map<String, String>) field.get(scoringService);
        tokenMap.put(token, customerNumber);
        
        when(customerService.findByCustomerNumber(customerNumber)).thenReturn(Optional.of(customer));
        when(loanRepository.findByCustomer(customer)).thenReturn(java.util.List.of(pendingLoan));
        doNothing().when(loanService).updateLoanWithScoreAndLimit(pendingLoan, score, limit);

        // Act
        Loan result = scoringService.processScoreCallback(token, score, limit);

        // Assert
        assertNotNull(result);
        assertEquals(customer, result.getCustomer());
        verify(customerService, times(1)).findByCustomerNumber(customerNumber);
        verify(loanRepository, times(1)).findByCustomer(customer);
        verify(loanService, times(1)).updateLoanWithScoreAndLimit(pendingLoan, score, limit);
        
        // Verify token was removed
        assertFalse(tokenMap.containsKey(token));
    }

    @Test
    public void processScoreCallback_invalidToken() {
        // Arrange
        String token = "invalid-token";
        Double score = 750.0;
        Double limit = 10000.0;

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            scoringService.processScoreCallback(token, score, limit);
        });
        
        assertEquals("Invalid token", exception.getMessage());
        verify(customerService, never()).findByCustomerNumber(any());
        verify(loanRepository, never()).findByCustomer(any());
        verify(loanService, never()).updateLoanWithScoreAndLimit(any(), any(), any());
    }

    @Test
    public void scoreLoan_success() {
        // Arrange
        Loan loan = new Loan();
        loan.setStatus(LoanStatus.PENDING.name());
        
        doNothing().when(loanService).updateLoanWithScoreAndLimit(any(Loan.class), anyDouble(), anyDouble());

        // Act
        scoringService.scoreLoan(loan);

        // Assert
        verify(loanService, times(1)).updateLoanWithScoreAndLimit(eq(loan), anyDouble(), anyDouble());
    }
}

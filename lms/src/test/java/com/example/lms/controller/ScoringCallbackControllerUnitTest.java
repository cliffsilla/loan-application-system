package com.example.lms.controller;

import com.example.lms.dto.ScoringCallbackRequest;
import com.example.lms.entity.Customer;
import com.example.lms.entity.Loan;
import com.example.lms.entity.LoanStatus;
import com.example.lms.service.ScoringService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScoringCallbackControllerUnitTest {

    @Mock
    private ScoringService scoringService;

    @InjectMocks
    private ScoringCallbackController scoringCallbackController;

    @Test
    public void processCallback_success() {
        // Arrange
        ScoringCallbackRequest request = new ScoringCallbackRequest();
        request.setToken(UUID.randomUUID().toString());
        request.setScore(750.0);
        request.setLimit(10000.0);
        
        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setCustomerNumber("12345");
        
        Loan loan = new Loan();
        loan.setLoanId(UUID.randomUUID());
        loan.setCustomer(customer);
        loan.setAmount(5000.0);
        loan.setStatus(LoanStatus.APPROVED.name());
        loan.setScore(request.getScore());
        loan.setLimit(request.getLimit());
        
        when(scoringService.processScoreCallback(
                request.getToken(), 
                request.getScore(), 
                request.getLimit())
        ).thenReturn(loan);

        // Act
        ResponseEntity<?> response = scoringCallbackController.processCallback(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(scoringService, times(1)).processScoreCallback(
                request.getToken(), 
                request.getScore(), 
                request.getLimit());
    }

    @Test
    public void processCallback_error() {
        // Arrange
        ScoringCallbackRequest request = new ScoringCallbackRequest();
        request.setToken(UUID.randomUUID().toString());
        request.setScore(750.0);
        request.setLimit(10000.0);
        
        when(scoringService.processScoreCallback(anyString(), anyDouble(), anyDouble()))
            .thenThrow(new RuntimeException("Test error"));

        // Act
        ResponseEntity<?> response = scoringCallbackController.processCallback(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(scoringService, times(1)).processScoreCallback(
                request.getToken(), 
                request.getScore(), 
                request.getLimit());
    }
}

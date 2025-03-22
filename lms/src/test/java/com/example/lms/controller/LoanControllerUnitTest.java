package com.example.lms.controller;

import com.example.lms.dto.LoanRequest;
import com.example.lms.entity.Customer;
import com.example.lms.entity.Loan;
import com.example.lms.entity.LoanStatus;
import com.example.lms.service.LoanService;
import com.example.lms.service.ScoringService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanControllerUnitTest {

    @Mock
    private LoanService loanService;

    @Mock
    private ScoringService scoringService;

    @InjectMocks
    private LoanController loanController;

    @Test
    public void applyForLoan_success() {
        // Arrange
        LoanRequest request = new LoanRequest();
        request.setCustomerNumber("12345");
        request.setAmount(5000.0);
        
        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setCustomerNumber("12345");
        
        Loan loan = new Loan();
        loan.setLoanId(UUID.randomUUID());
        loan.setCustomer(customer);
        loan.setAmount(5000.0);
        loan.setStatus(LoanStatus.PENDING.name());
        
        when(loanService.createLoanApplication(request.getCustomerNumber(), request.getAmount())).thenReturn(loan);
        doNothing().when(scoringService).scoreLoan(loan);

        // Act
        ResponseEntity<?> response = loanController.applyForLoan(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(loanService, times(1)).createLoanApplication(request.getCustomerNumber(), request.getAmount());
        verify(scoringService, times(1)).scoreLoan(loan);
    }

    @Test
    public void applyForLoan_error() {
        // Arrange
        LoanRequest request = new LoanRequest();
        request.setCustomerNumber("12345");
        request.setAmount(5000.0);
        
        when(loanService.createLoanApplication(anyString(), anyDouble()))
            .thenThrow(new RuntimeException("Test error"));

        // Act
        ResponseEntity<?> response = loanController.applyForLoan(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(loanService, times(1)).createLoanApplication(request.getCustomerNumber(), request.getAmount());
        verify(scoringService, never()).scoreLoan(any());
    }

    @Test
    public void getLoanStatus_success() {
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
        loan.setScore(750.0);
        loan.setLimit(10000.0);
        
        when(loanService.getLoanStatus(loanId)).thenReturn(Optional.of(loan));

        // Act
        ResponseEntity<?> response = loanController.getLoanStatus(loanId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(loanService, times(1)).getLoanStatus(loanId);
    }

    @Test
    public void getLoanStatus_notFound() {
        // Arrange
        UUID loanId = UUID.randomUUID();
        when(loanService.getLoanStatus(loanId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = loanController.getLoanStatus(loanId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(loanService, times(1)).getLoanStatus(loanId);
    }
}

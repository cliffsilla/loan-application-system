package com.example.lms.service;

import com.example.lms.entity.Customer;
import com.example.lms.entity.Loan;
import com.example.lms.entity.LoanStatus;
import com.example.lms.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanServiceUnitTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private LoanServiceImpl loanService;

    @Test
    public void createLoanApplication_success() {
        // Arrange
        String customerNumber = "12345";
        Double amount = 5000.0;
        
        Customer customer = new Customer();
        customer.setCustomerNumber(customerNumber);
        
        Loan savedLoan = new Loan();
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
    public void createLoanApplication_customerNotSubscribed() {
        // Arrange
        String customerNumber = "12345";
        Double amount = 5000.0;
        
        when(customerService.findByCustomerNumber(customerNumber)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loanService.createLoanApplication(customerNumber, amount);
        });
        
        assertEquals("Customer not subscribed", exception.getMessage());
        verify(customerService, times(1)).findByCustomerNumber(customerNumber);
        verify(loanRepository, never()).existsByCustomerAndStatusIn(any(), any());
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    public void createLoanApplication_existingPendingLoan() {
        // Arrange
        String customerNumber = "12345";
        Double amount = 5000.0;
        
        Customer customer = new Customer();
        customer.setCustomerNumber(customerNumber);
        
        List<String> activeStatuses = Arrays.asList(
                LoanStatus.PENDING.name(),
                LoanStatus.APPROVED.name(),
                LoanStatus.ACTIVE.name()
        );
        
        when(customerService.findByCustomerNumber(customerNumber)).thenReturn(Optional.of(customer));
        when(loanRepository.existsByCustomerAndStatusIn(eq(customer), eq(activeStatuses))).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loanService.createLoanApplication(customerNumber, amount);
        });
        
        assertEquals("Customer has existing loan", exception.getMessage());
        verify(customerService, times(1)).findByCustomerNumber(customerNumber);
        verify(loanRepository, times(1)).existsByCustomerAndStatusIn(eq(customer), eq(activeStatuses));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    public void getLoanStatus_success() {
        // Arrange
        UUID loanId = UUID.randomUUID();
        Loan loan = new Loan();
        loan.setLoanId(loanId);
        
        when(loanRepository.findByLoanId(loanId)).thenReturn(Optional.of(loan));

        // Act
        Optional<Loan> result = loanService.getLoanStatus(loanId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(loanId, result.get().getLoanId());
        verify(loanRepository, times(1)).findByLoanId(loanId);
    }

    @Test
    public void getLoanStatus_notFound() {
        // Arrange
        UUID loanId = UUID.randomUUID();
        when(loanRepository.findByLoanId(loanId)).thenReturn(Optional.empty());

        // Act
        Optional<Loan> result = loanService.getLoanStatus(loanId);

        // Assert
        assertFalse(result.isPresent());
        verify(loanRepository, times(1)).findByLoanId(loanId);
    }

    @Test
    public void updateLoanWithScoreAndLimit_approved() {
        // Arrange
        Loan loan = new Loan();
        loan.setAmount(5000.0);
        loan.setStatus(LoanStatus.PENDING.name());
        
        Double score = 750.0;
        Double limit = 10000.0;

        // Act
        loanService.updateLoanWithScoreAndLimit(loan, score, limit);

        // Assert
        assertEquals(score, loan.getScore());
        assertEquals(limit, loan.getLimit());
        assertEquals(LoanStatus.APPROVED.name(), loan.getStatus());
        assertNull(loan.getRejectionReason());
        verify(loanRepository, times(1)).save(loan);
    }

    @Test
    public void updateLoanWithScoreAndLimit_rejectedLowScore() {
        // Arrange
        Loan loan = new Loan();
        loan.setAmount(5000.0);
        loan.setStatus(LoanStatus.PENDING.name());
        
        Double score = 650.0; // Below 700
        Double limit = 10000.0;

        // Act
        loanService.updateLoanWithScoreAndLimit(loan, score, limit);

        // Assert
        assertEquals(score, loan.getScore());
        assertEquals(limit, loan.getLimit());
        assertEquals(LoanStatus.REJECTED.name(), loan.getStatus());
        assertEquals("Credit score too low", loan.getRejectionReason());
        verify(loanRepository, times(1)).save(loan);
    }

    @Test
    public void updateLoanWithScoreAndLimit_rejectedLowLimit() {
        // Arrange
        Loan loan = new Loan();
        loan.setAmount(5000.0);
        loan.setStatus(LoanStatus.PENDING.name());
        
        Double score = 750.0; // Above 700
        Double limit = 4000.0; // Below requested amount

        // Act
        loanService.updateLoanWithScoreAndLimit(loan, score, limit);

        // Assert
        assertEquals(score, loan.getScore());
        assertEquals(limit, loan.getLimit());
        assertEquals(LoanStatus.REJECTED.name(), loan.getStatus());
        assertEquals("Requested amount exceeds approved limit", loan.getRejectionReason());
        verify(loanRepository, times(1)).save(loan);
    }
}

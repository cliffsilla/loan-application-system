package com.example.lms.service;

import com.example.lms.entity.Customer;
import com.example.lms.repository.CustomerRepository;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageChannel;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceUnitTest {

    @Mock
    private CustomerRepository customerRepository;
    
    @Mock
    private MessageChannel cbsRequestChannel;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    public void subscribeCustomer_success() throws JAXBException {
        // Arrange
        String customerNumber = "12345";
        Customer savedCustomer = new Customer();
        savedCustomer.setCustomerNumber(customerNumber);
        
        when(customerRepository.findByCustomerNumber(customerNumber)).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(cbsRequestChannel.send(any())).thenReturn(true);

        // Act
        Customer result = customerService.subscribeCustomer(customerNumber);

        // Assert
        assertNotNull(result);
        assertEquals(customerNumber, result.getCustomerNumber());
        verify(customerRepository, times(1)).findByCustomerNumber(customerNumber);
        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(cbsRequestChannel, times(1)).send(any());
    }

    @Test
    public void subscribeCustomer_customerAlreadyExists() throws JAXBException {
        // Arrange
        String customerNumber = "12345";
        Customer existingCustomer = new Customer();
        existingCustomer.setCustomerNumber(customerNumber);
        
        when(customerRepository.findByCustomerNumber(customerNumber)).thenReturn(Optional.of(existingCustomer));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> customerService.subscribeCustomer(customerNumber));
        verify(customerRepository, times(1)).findByCustomerNumber(customerNumber);
        verify(customerRepository, never()).save(any(Customer.class));
        verify(cbsRequestChannel, never()).send(any());
    }

    @Test
    public void findByCustomerNumber_success() {
        // Arrange
        String customerNumber = "12345";
        Customer customer = new Customer();
        customer.setCustomerNumber(customerNumber);
        
        when(customerRepository.findByCustomerNumber(customerNumber)).thenReturn(Optional.of(customer));

        // Act
        Optional<Customer> result = customerService.findByCustomerNumber(customerNumber);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(customerNumber, result.get().getCustomerNumber());
        verify(customerRepository, times(1)).findByCustomerNumber(customerNumber);
    }

    @Test
    public void findByCustomerNumber_notFound() {
        // Arrange
        String customerNumber = "12345";
        when(customerRepository.findByCustomerNumber(customerNumber)).thenReturn(Optional.empty());

        // Act
        Optional<Customer> result = customerService.findByCustomerNumber(customerNumber);

        // Assert
        assertFalse(result.isPresent());
        verify(customerRepository, times(1)).findByCustomerNumber(customerNumber);
    }

    @Test
    public void getCustomerKycData_returnsValidJson() throws JAXBException {
        // Arrange
        String customerNumber = "12345";
        when(cbsRequestChannel.send(any())).thenReturn(true);

        // Act
        String result = customerService.getCustomerKycData(customerNumber);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("firstName"));
        assertTrue(result.contains("lastName"));
        verify(cbsRequestChannel, times(1)).send(any());
    }
}

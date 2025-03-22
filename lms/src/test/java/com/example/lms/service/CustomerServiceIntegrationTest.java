package com.example.lms.service;

import com.example.lms.entity.Customer;
import com.example.lms.repository.CustomerRepository;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class CustomerServiceIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @MockBean
    private CustomerRepository customerRepository;
    
    @MockBean
    private MessageChannel cbsRequestChannel;

    @Test
    public void subscribeCustomer_integration() throws JAXBException {
        // Arrange
        String customerNumber = "12345";
        Customer savedCustomer = new Customer();
        savedCustomer.setCustomerId(UUID.randomUUID());
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
    public void findByCustomerNumber_integration() {
        // Arrange
        String customerNumber = "12345";
        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
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
    public void getCustomerKycData_integration() throws JAXBException {
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

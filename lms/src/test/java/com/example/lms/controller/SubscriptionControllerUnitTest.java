package com.example.lms.controller;

import com.example.lms.dto.SubscriptionRequest;
import com.example.lms.entity.Customer;
import com.example.lms.service.CustomerService;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionControllerUnitTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private SubscriptionController subscriptionController;

    @Test
    public void subscribeCustomer_success() throws JAXBException {
        // Arrange
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerNumber("12345");
        
        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setCustomerNumber("12345");
        
        when(customerService.subscribeCustomer(request.getCustomerNumber())).thenReturn(customer);

        // Act
        ResponseEntity<?> response = subscriptionController.subscribeCustomer(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(customerService, times(1)).subscribeCustomer(request.getCustomerNumber());
    }

    @Test
    public void subscribeCustomer_jaxbException() throws JAXBException {
        // Arrange
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerNumber("12345");
        
        when(customerService.subscribeCustomer(anyString())).thenThrow(new JAXBException("JAXB parsing error"));

        // Act
        ResponseEntity<?> response = subscriptionController.subscribeCustomer(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(customerService, times(1)).subscribeCustomer(request.getCustomerNumber());
    }

    @Test
    public void subscribeCustomer_error() throws JAXBException {
        // Arrange
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerNumber("12345");
        
        when(customerService.subscribeCustomer(anyString())).thenThrow(new RuntimeException("Test error"));

        // Act
        ResponseEntity<?> response = subscriptionController.subscribeCustomer(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(customerService, times(1)).subscribeCustomer(request.getCustomerNumber());
    }
}

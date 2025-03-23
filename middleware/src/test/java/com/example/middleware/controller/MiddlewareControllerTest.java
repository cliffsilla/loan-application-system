package com.example.middleware.controller;

import com.example.middleware.service.ScoringEngineService;
import com.example.middleware.service.TransactionDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class MiddlewareControllerTest {

    @Mock
    private TransactionDataService transactionDataService;

    @Mock
    private ScoringEngineService scoringEngineService;

    @InjectMocks
    private MiddlewareController middlewareController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test the transaction data endpoint with multiple customer IDs
     */
    @ParameterizedTest
    @ValueSource(strings = {"234774784", "318411216", "340397370", "366585630", "397178638"})
    void getTransactionDataWithMultipleCustomerIds(String customerId) {
        // Create mock response
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("customerNumber", customerId);
        mockResponse.put("transactions", new HashMap<>());

        // Mock the service
        when(transactionDataService.getTransactionData(customerId)).thenReturn(mockResponse);

        // Call the controller method
        ResponseEntity<Map<String, Object>> response = middlewareController.getTransactionData(customerId);

        // Verify the response
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(customerId, response.getBody().get("customerNumber"));
    }

    /**
     * Test the customer score endpoint with multiple customer IDs
     */
    @ParameterizedTest
    @ValueSource(strings = {"234774784", "318411216", "340397370", "366585630", "397178638"})
    void getCustomerScoreWithMultipleCustomerIds(String customerId) {
        // Create mock response
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("customerNumber", customerId);
        mockResponse.put("score", 750);

        // Mock the service
        when(scoringEngineService.getCustomerScore(customerId)).thenReturn(mockResponse);

        // Call the controller method
        ResponseEntity<Map<String, Object>> response = middlewareController.getCustomerScore(customerId);

        // Verify the response
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(customerId, response.getBody().get("customerNumber"));
        assertEquals(750, response.getBody().get("score"));
    }

    @Test
    void registerClient() {
        // Create client data with one of the test customer IDs
        Map<String, Object> clientData = new HashMap<>();
        clientData.put("clientId", "234774784");
        clientData.put("name", "Test Client");

        // Create mock response
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("clientId", "234774784");

        // Mock the service
        when(scoringEngineService.registerClient(clientData)).thenReturn(mockResponse);

        // Call the controller method
        ResponseEntity<Map<String, Object>> response = middlewareController.registerClient(clientData);

        // Verify the response
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().get("success"));
        assertEquals("234774784", response.getBody().get("clientId"));
    }
}

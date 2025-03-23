package com.example.middleware.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class TransactionDataServiceTest {

    @Mock
    private WebServiceTemplate webServiceTemplate;

    @InjectMocks
    private TransactionDataService transactionDataService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up properties
        ReflectionTestUtils.setField(transactionDataService, "username", "admin");
        ReflectionTestUtils.setField(transactionDataService, "password", "pwd123");
        ReflectionTestUtils.setField(transactionDataService, "transactionUrl", "https://test-url.com/service");
    }

    /**
     * Test with multiple customer IDs to ensure the service handles different inputs correctly
     */
    @ParameterizedTest
    @ValueSource(strings = {"234774784", "318411216", "340397370", "366585630", "397178638"})
    void getTransactionDataWithMultipleCustomerIds(String customerId) {
        // Mock the WebServiceTemplate to simulate an exception (to test fallback)
        when(webServiceTemplate.marshalSendAndReceive(
                anyString(),
                any(),
                any(SoapActionCallback.class)
        )).thenThrow(new RuntimeException("Test exception"));

        // Call the service method
        Map<String, Object> result = transactionDataService.getTransactionData(customerId);

        // Verify the fallback response
        assertNotNull(result);
        assertEquals(customerId, result.get("customerNumber"));
        assertEquals(true, result.get("isFallback"));
        assertNotNull(result.get("transactions"));
    }

    /**
     * Test successful response handling
     */
    @Test
    void getTransactionDataSuccess() {
        // Mock the WebServiceTemplate to return a successful response
        String mockResponse = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soap:Body>" +
                "<GetTransactionDataResponse xmlns=\"http://credable.io/cbs/\">" +
                "<customerNumber>234774784</customerNumber>" +
                "<transactionCount>5</transactionCount>" +
                "</GetTransactionDataResponse>" +
                "</soap:Body>" +
                "</soap:Envelope>";
        
        when(webServiceTemplate.marshalSendAndReceive(
                anyString(),
                any(),
                any(SoapActionCallback.class)
        )).thenReturn(mockResponse);

        // Call the service method
        Map<String, Object> result = transactionDataService.getTransactionData("234774784");

        // Verify the response
        assertNotNull(result);
        assertEquals("234774784", result.get("customerNumber"));
        assertFalse(result.containsKey("isFallback"));
        assertNotNull(result.get("transactions"));
    }
}

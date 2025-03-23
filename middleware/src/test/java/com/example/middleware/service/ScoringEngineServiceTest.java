package com.example.middleware.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ScoringEngineServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ScoringEngineService scoringEngineService;
    
    private final ParameterizedTypeReference<Map<String, Object>> mapTypeReference = 
            new ParameterizedTypeReference<>() {};

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up properties
        ReflectionTestUtils.setField(scoringEngineService, "scoringEngineUrl", "https://test-url.com/api");
        ReflectionTestUtils.setField(scoringEngineService, "username", "admin");
        ReflectionTestUtils.setField(scoringEngineService, "password", "pwd123");
        ReflectionTestUtils.setField(scoringEngineService, "maxRetryAttempts", 3);
        ReflectionTestUtils.setField(scoringEngineService, "retryDelayMs", 10L); // Short delay for tests
    }

    /**
     * Test with multiple customer IDs to ensure the service handles different inputs correctly
     */
    @ParameterizedTest
    @ValueSource(strings = {"234774784", "318411216", "340397370", "366585630", "397178638"})
    void getCustomerScoreWithMultipleCustomerIds(String customerId) {
        // Create a mock response
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("customerNumber", customerId);
        responseBody.put("score", 750);
        responseBody.put("scoreDate", "2025-03-22");
        
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        
        // Mock the RestTemplate to return our response
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(mapTypeReference)
        )).thenReturn(responseEntity);

        // Call the service method
        Map<String, Object> result = scoringEngineService.getCustomerScore(customerId);

        // Verify the result
        assertNotNull(result);
        assertEquals(customerId, result.get("customerNumber"));
        assertEquals(750, result.get("score"));
        assertEquals("2025-03-22", result.get("scoreDate"));
        
        // Verify the RestTemplate was called once (no retries needed)
        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(mapTypeReference)
        );
    }

    /**
     * Test the retry mechanism when the first call fails but subsequent calls succeed
     */
    @Test
    void getCustomerScoreWithRetrySuccess() {
        // Mock the RestTemplate to fail on first call, succeed on second
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(mapTypeReference)
        ))
        .thenThrow(new RestClientException("Test exception"))
        .thenAnswer(invocation -> {
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("customerNumber", "234774784");
            responseBody.put("score", 750);
            responseBody.put("scoreDate", "2025-03-22");
            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        });

        // Call the service method
        Map<String, Object> result = scoringEngineService.getCustomerScore("234774784");

        // Verify the result
        assertNotNull(result);
        assertEquals("234774784", result.get("customerNumber"));
        assertEquals(750, result.get("score"));
        
        // Verify the RestTemplate was called twice (one failure, one success)
        verify(restTemplate, times(2)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(mapTypeReference)
        );
    }

    /**
     * Test that the service returns a fallback response after all retry attempts fail
     */
    @Test
    void getCustomerScoreWithAllRetriesFailing() {
        // Mock the RestTemplate to always throw an exception
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(mapTypeReference)
        )).thenThrow(new RestClientException("Test exception"));

        // Call the service method
        Map<String, Object> result = scoringEngineService.getCustomerScore("234774784");

        // Verify the result is a fallback response
        assertNotNull(result);
        assertEquals("234774784", result.get("customerNumber"));
        assertEquals(true, result.get("isFallback"));
        
        // Verify the RestTemplate was called maxRetryAttempts times (3 in this case)
        verify(restTemplate, times(3)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(mapTypeReference)
        );
    }

    /**
     * Test client registration functionality
     */
    @Test
    void registerClientTest() {
        // Create client data
        Map<String, Object> clientData = new HashMap<>();
        clientData.put("clientId", "234774784");
        clientData.put("firstName", "John");
        clientData.put("lastName", "Doe");
        
        // Create a mock response
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        responseBody.put("clientId", "234774784");
        
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        
        // Mock the RestTemplate
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(mapTypeReference)
        )).thenReturn(responseEntity);

        // Call the service method
        Map<String, Object> result = scoringEngineService.registerClient(clientData);

        // Verify the result
        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertEquals("234774784", result.get("clientId"));
    }

    /**
     * Test client registration with multiple customer IDs
     */
    @ParameterizedTest
    @ValueSource(strings = {"234774784", "318411216", "340397370", "366585630", "397178638"})
    void registerClientWithMultipleCustomerIds(String clientId) {
        // Create client data
        Map<String, Object> clientData = new HashMap<>();
        clientData.put("clientId", clientId);
        clientData.put("firstName", "John");
        clientData.put("lastName", "Doe");
        
        // Create a mock response
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        responseBody.put("clientId", clientId);
        
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        
        // Mock the RestTemplate
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(mapTypeReference)
        )).thenReturn(responseEntity);

        // Call the service method
        Map<String, Object> result = scoringEngineService.registerClient(clientData);

        // Verify the result
        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertEquals(clientId, result.get("clientId"));
    }
}

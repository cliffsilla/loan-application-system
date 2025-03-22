package com.example.middleware.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ScoringEngineService {

    private final RestTemplate restTemplate;
    
    @Value("${com.example.middleware.scoring.engine.url:https://scoreapitest.credable.io/api}")
    private String scoringEngineUrl;
    
    @Value("${scoring.engine.username:admin}")
    private String username;
    
    @Value("${scoring.engine.password:pwd123}")
    private String password;
    
    @Value("${scoring.engine.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${scoring.engine.retry.delay-ms:1000}")
    private long retryDelayMs;
    
    public ScoringEngineService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Retrieves a customer score from the Scoring Engine with retry mechanism
     * 
     * @param customerNumber The customer number
     * @return The score response
     */
    public Map<String, Object> getCustomerScore(String customerNumber) {
        String url = scoringEngineUrl + "/score/" + customerNumber;
        
        // Create request with authentication
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        Exception lastException = null;
        
        // Implement retry mechanism
        for (int attempt = 0; attempt < maxRetryAttempts; attempt++) {
            try {
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<Map<String, Object>>() {}
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return response.getBody();
                }
                
            } catch (RestClientException e) {
                lastException = e;
                System.err.println("Attempt " + (attempt + 1) + " failed: " + e.getMessage());
                
                // Wait before retrying
                try {
                    Thread.sleep(retryDelayMs * (attempt + 1)); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        // If all retries failed, return fallback response
        System.err.println("All retry attempts failed. Last error: " + 
                (lastException != null ? lastException.getMessage() : "Unknown error"));
        return createFallbackScoreResponse(customerNumber);
    }
    
    /**
     * Registers a client with the Scoring Engine
     * 
     * @param clientData The client registration data
     * @return The registration response
     */
    public Map<String, Object> registerClient(Map<String, Object> clientData) {
        String url = scoringEngineUrl + "/client/register";
        
        // Create request with authentication
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(clientData, headers);
        
        Exception lastException = null;
        
        // Implement retry mechanism
        for (int attempt = 0; attempt < maxRetryAttempts; attempt++) {
            try {
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        requestEntity,
                        new ParameterizedTypeReference<Map<String, Object>>() {}
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return response.getBody();
                }
                
            } catch (RestClientException e) {
                lastException = e;
                System.err.println("Attempt " + (attempt + 1) + " failed: " + e.getMessage());
                
                // Wait before retrying
                try {
                    Thread.sleep(retryDelayMs * (attempt + 1)); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        // If all retries failed, return error response
        System.err.println("All retry attempts failed. Last error: " + 
                (lastException != null ? lastException.getMessage() : "Unknown error"));
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "Failed to register client after " + maxRetryAttempts + " attempts");
        return errorResponse;
    }
    
    /**
     * Registers a client with the Scoring Engine using the createClient endpoint
     * 
     * @param clientData The client registration data with url, name, username, and password
     * @return The registration response
     */
    public Map<String, Object> createClient(Map<String, Object> clientData) {
        String url = scoringEngineUrl + "/client/createClient";
        
        // Validate required fields
        if (!clientData.containsKey("url") || !clientData.containsKey("name") || 
            !clientData.containsKey("username") || !clientData.containsKey("password")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Missing required fields: url, name, username, and password are required");
            return errorResponse;
        }
        
        // Create request with authentication
        HttpHeaders headers = createAuthHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(clientData, headers);
        
        Exception lastException = null;
        
        // Implement retry mechanism
        for (int attempt = 0; attempt < maxRetryAttempts; attempt++) {
            try {
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        requestEntity,
                        new ParameterizedTypeReference<Map<String, Object>>() {}
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return response.getBody();
                }
                
            } catch (RestClientException e) {
                lastException = e;
                System.err.println("Attempt " + (attempt + 1) + " failed: " + e.getMessage());
                
                // Wait before retrying
                try {
                    Thread.sleep(retryDelayMs * (attempt + 1)); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        // If all retries failed, return error response
        System.err.println("All retry attempts failed. Last error: " + 
                (lastException != null ? lastException.getMessage() : "Unknown error"));
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "Failed to create client after " + maxRetryAttempts + " attempts");
        return errorResponse;
    }
    
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        return headers;
    }
    
    private Map<String, Object> createFallbackScoreResponse(String customerNumber) {
        Map<String, Object> response = new HashMap<>();
        response.put("customerNumber", customerNumber);
        response.put("score", 650); // Default fallback score
        response.put("scoreDate", java.time.LocalDate.now().toString());
        response.put("isFallback", true);
        response.put("message", "This is fallback data due to Scoring Engine API failure");
        return response;
    }
}

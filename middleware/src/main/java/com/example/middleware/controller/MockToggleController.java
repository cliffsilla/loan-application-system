package com.example.middleware.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller to provide information about mock services status.
 * This controller is available in all profiles to help identify when mock services are active.
 */
@RestController
@RequestMapping("/mock-status")
public class MockToggleController {

    private static final Logger logger = LoggerFactory.getLogger(MockToggleController.class);
    
    @Value("${spring.profiles.active:default}")
    private String activeProfiles;
    
    /**
     * Get the current mock services status
     * @return Response with mock services status information
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMockStatus() {
        boolean mockActive = activeProfiles.contains("mock");
        
        Map<String, Object> response = new HashMap<>();
        response.put("mockServicesActive", mockActive);
        response.put("activeProfiles", activeProfiles);
        
        if (mockActive) {
            response.put("scoringEngineUrl", "mock://scoring-engine");
            response.put("coreBankingUrl", "mock://core-banking");
            response.put("message", "Mock services are active. External systems are being simulated.");
        } else {
            response.put("message", "Mock services are inactive. Real external systems are being used.");
        }
        
        logger.info("Mock status requested: mockServicesActive={}", mockActive);
        return ResponseEntity.ok(response);
    }
}

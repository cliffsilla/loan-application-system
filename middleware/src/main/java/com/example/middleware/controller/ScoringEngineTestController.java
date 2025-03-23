package com.example.middleware.controller;

import com.example.middleware.service.ScoringEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for testing the scoring engine connection
 */
@RestController
@RequestMapping("/scoring-engine")
public class ScoringEngineTestController {

    private final ScoringEngineService scoringEngineService;

    @Autowired
    public ScoringEngineTestController(ScoringEngineService scoringEngineService) {
        this.scoringEngineService = scoringEngineService;
    }

    /**
     * Test the connection to the scoring engine
     * @return A response indicating if the connection was successful
     */
    @GetMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test the connection by calling a simple method on the scoring engine
            boolean connectionSuccessful = scoringEngineService.testConnection();
            
            if (connectionSuccessful) {
                response.put("status", "success");
                response.put("message", "Successfully connected to the scoring engine");
                response.put("scoringEngineUrl", scoringEngineService.getScoringEngineUrl());
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Failed to connect to the scoring engine");
                response.put("scoringEngineUrl", scoringEngineService.getScoringEngineUrl());
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error testing connection to scoring engine: " + e.getMessage());
            response.put("scoringEngineUrl", scoringEngineService.getScoringEngineUrl());
            response.put("errorDetails", e.toString());
            return ResponseEntity.status(500).body(response);
        }
    }
}

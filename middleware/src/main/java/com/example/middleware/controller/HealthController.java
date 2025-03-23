package com.example.middleware.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for health checks
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * Simple health check endpoint
     * @return A response indicating the service is up
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "middleware");
        return ResponseEntity.ok(response);
    }
}

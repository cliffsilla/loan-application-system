package com.example.middleware.controller;

import com.example.middleware.service.ScoringEngineService;
import com.example.middleware.service.TransactionDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class MiddlewareController {

    private final TransactionDataService transactionDataService;
    private final ScoringEngineService scoringEngineService;

    public MiddlewareController(TransactionDataService transactionDataService, 
                               ScoringEngineService scoringEngineService) {
        this.transactionDataService = transactionDataService;
        this.scoringEngineService = scoringEngineService;
    }

    @GetMapping("/transactions/{customerNumber}")
    public ResponseEntity<Map<String, Object>> getTransactionData(@PathVariable String customerNumber) {
        Map<String, Object> response = transactionDataService.getTransactionData(customerNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/score/{customerNumber}")
    public ResponseEntity<Map<String, Object>> getCustomerScore(@PathVariable String customerNumber) {
        Map<String, Object> response = scoringEngineService.getCustomerScore(customerNumber);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/client/register")
    public ResponseEntity<Map<String, Object>> registerClient(@RequestBody Map<String, Object> clientData) {
        Map<String, Object> response = scoringEngineService.registerClient(clientData);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/client/create")
    public ResponseEntity<Map<String, Object>> createClient(@RequestBody Map<String, Object> clientData) {
        Map<String, Object> response = scoringEngineService.createClient(clientData);
        return ResponseEntity.ok(response);
    }
}

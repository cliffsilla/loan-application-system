package com.example.middleware.controller;

import com.example.middleware.service.TransactionDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionDataController {

    private final TransactionDataService transactionDataService;

    @GetMapping("/{customerNumber}")
    public ResponseEntity<Map<String, Object>> getTransactionData(@PathVariable String customerNumber) {
        Map<String, Object> transactionData = transactionDataService.getTransactionData(customerNumber);
        return ResponseEntity.ok(transactionData);
    }
}

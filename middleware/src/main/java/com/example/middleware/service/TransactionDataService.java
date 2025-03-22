package com.example.middleware.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionDataService {

    /**
     * Retrieves transaction data for a customer
     * In a real implementation, this would call a database or external service
     * For now, we'll return mock data
     * 
     * @param customerNumber The customer number
     * @return A map containing transaction data
     */
    public Map<String, Object> getTransactionData(String customerNumber) {
        Map<String, Object> response = new HashMap<>();
        
        // Add customer info
        response.put("customerNumber", customerNumber);
        
        // Create mock transactions
        List<Map<String, Object>> transactions = new ArrayList<>();
        
        // Add some sample transactions
        transactions.add(createTransaction("TXN001", "DEBIT", 1500.0, "2025-03-01", "Supermarket"));
        transactions.add(createTransaction("TXN002", "CREDIT", 5000.0, "2025-03-05", "Salary"));
        transactions.add(createTransaction("TXN003", "DEBIT", 2000.0, "2025-03-10", "Rent"));
        transactions.add(createTransaction("TXN004", "DEBIT", 500.0, "2025-03-15", "Restaurant"));
        transactions.add(createTransaction("TXN005", "DEBIT", 300.0, "2025-03-18", "Utilities"));
        
        response.put("transactions", transactions);
        response.put("count", transactions.size());
        
        return response;
    }
    
    private Map<String, Object> createTransaction(String id, String type, double amount, String date, String description) {
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("id", id);
        transaction.put("type", type);
        transaction.put("amount", amount);
        transaction.put("date", date);
        transaction.put("description", description);
        return transaction;
    }
}

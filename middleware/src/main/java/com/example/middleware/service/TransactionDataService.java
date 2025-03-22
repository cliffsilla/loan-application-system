package com.example.middleware.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import java.util.HashMap;
import java.util.Map;

@Service
public class TransactionDataService {

    private final WebServiceTemplate webServiceTemplate;
    
    @Value("${cbs.username:admin}")
    private String username;
    
    @Value("${cbs.password:pwd123}")
    private String password;
    
    @Value("${com.example.middleware.transaction.data.url:https://trxapitest.credable.io/service/transaction}")
    private String transactionUrl;
    
    public TransactionDataService(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }
    
    /**
     * Retrieves transaction data for a customer from the CBS
     * 
     * @param customerNumber The customer number
     * @return The transaction data
     */
    public Map<String, Object> getTransactionData(String customerNumber) {
        try {
            // Create the request
            String soapRequest = createSoapRequest(customerNumber);
            
            // Add authentication interceptor
            webServiceTemplate.setInterceptors(new ClientInterceptor[] {
                new BasicAuthenticationInterceptor(username, password)
            });
            
            // Make the SOAP call
            String response = (String) webServiceTemplate.marshalSendAndReceive(
                    transactionUrl,
                    soapRequest,
                    new SoapActionCallback("http://credable.io/cbs/GetTransactionData")
            );
            
            // Process the response
            return processResponse(response, customerNumber);
            
        } catch (Exception e) {
            System.err.println("Error calling CBS Transaction API: " + e.getMessage());
            // Return fallback data
            return createFallbackResponse(customerNumber);
        }
    }
    
    private String createSoapRequest(String customerNumber) {
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                "xmlns:cbs=\"http://credable.io/cbs/\">" +
                "<soapenv:Header/>" +
                "<soapenv:Body>" +
                "<cbs:GetTransactionDataRequest>" +
                "<cbs:customerNumber>" + customerNumber + "</cbs:customerNumber>" +
                "</cbs:GetTransactionDataRequest>" +
                "</soapenv:Body>" +
                "</soapenv:Envelope>";
    }
    
    private Map<String, Object> processResponse(String response, String customerNumber) {
        // In a real implementation, this would parse the XML response
        // For this example, we'll create a sample response
        Map<String, Object> result = new HashMap<>();
        result.put("customerNumber", customerNumber);
        result.put("responseData", response);
        
        // Add some sample transaction data
        Map<String, Object> transactions = new HashMap<>();
        transactions.put("count", 5);
        transactions.put("lastTransactionDate", "2025-03-20");
        result.put("transactions", transactions);
        
        return result;
    }
    
    private Map<String, Object> createFallbackResponse(String customerNumber) {
        Map<String, Object> response = new HashMap<>();
        response.put("customerNumber", customerNumber);
        response.put("isFallback", true);
        response.put("message", "This is fallback data due to CBS API failure");
        
        // Add some fallback transaction data
        Map<String, Object> transactions = new HashMap<>();
        transactions.put("count", 0);
        transactions.put("lastTransactionDate", "N/A");
        response.put("transactions", transactions);
        
        return response;
    }
}

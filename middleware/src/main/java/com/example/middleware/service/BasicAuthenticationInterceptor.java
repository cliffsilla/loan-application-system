package com.example.middleware.service;

import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpComponentsConnection;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Interceptor for adding Basic Authentication to SOAP web service calls
 */
public class BasicAuthenticationInterceptor implements ClientInterceptor {

    private final String username;
    private final String password;

    public BasicAuthenticationInterceptor(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean handleRequest(MessageContext messageContext) {
        try {
            var transportContext = TransportContextHolder.getTransportContext();
            if (transportContext != null && transportContext.getConnection() instanceof HttpComponentsConnection) {
                var connection = (HttpComponentsConnection) transportContext.getConnection();
                String auth = username + ":" + password;
                byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
                String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.UTF_8);
                connection.addRequestHeader("Authorization", authHeader);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error adding authentication header: " + e.getMessage());
            // Continue with the request even if authentication fails
            return true;
        }
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext) {
        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Exception ex) {
        // No action needed
    }
}

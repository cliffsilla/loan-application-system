package com.example.tests.config;

public class TestConfig {
    // Base URLs
    public static final String LMS_BASE_URL = "https://lms-credible-assessment.fly.dev";
    public static final String MIDDLEWARE_BASE_URL = "https://middleware-credible-assessment.fly.dev";
    
    // API Keys
    public static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind0YWNtc2RpeXRxa2djd3lybWhxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDI3MDgzNzksImV4cCI6MjA1ODI4NDM3OX0.axPG6US-rZnOKEPu1gAbNhUbk09O0F7y-ZOFWjC-CoE";
    
    // Test Data
    public static final String TEST_CUSTOMER_NUMBER = "234774784"; // Default test customer
    public static final double TEST_LOAN_AMOUNT = 1000.00;
    
    // Additional test customer IDs
    public static final String[] TEST_CUSTOMER_IDS = {
        "234774784",
        "318411216",
        "340397370",
        "366585630",
        "397178638"
    };
}

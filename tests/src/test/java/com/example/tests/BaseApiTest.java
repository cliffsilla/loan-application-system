package com.example.tests;

import com.example.tests.config.TestConfig;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseApiTest {
    
    protected static RequestSpecification lmsRequestSpec;
    protected static RequestSpecification middlewareRequestSpec;
    
    @BeforeAll
    public static void setup() {
        // Configure LMS request specification
        lmsRequestSpec = new RequestSpecBuilder()
                .setBaseUri(TestConfig.LMS_BASE_URL)
                .setContentType(ContentType.JSON)
                .addHeader("Authorization", "API Key " + TestConfig.API_KEY)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
        
        // Configure Middleware request specification
        middlewareRequestSpec = new RequestSpecBuilder()
                .setBaseUri(TestConfig.MIDDLEWARE_BASE_URL)
                .setContentType(ContentType.JSON)
                .addHeader("Authorization", "API Key " + TestConfig.API_KEY)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }
}

package com.example.tests;

import com.example.tests.config.TestConfig;
import com.example.tests.models.Customer;
import com.example.tests.models.LoanRequest;
import com.example.tests.models.LoanResponse;
import com.example.tests.models.ScoreRequest;
import com.example.tests.models.ScoreResponse;
import io.restassured.RestAssured;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LmsApiTest extends BaseApiTest {

    private static String customerId;
    private static String loanId;

    @Test
    @Order(1)
    public void testCustomerSubscription() {
        // Create a customer subscription request
        Customer customer = Customer.builder()
                .customerNumber(TestConfig.TEST_CUSTOMER_NUMBER)
                .build();

        // Send the request and validate the response
        customerId = given()
                .spec(lmsRequestSpec)
                .body(customer)
                .when()
                .post("/subscriptions")
                .then()
                .statusCode(200)
                .body("customerId", notNullValue())
                .extract()
                .path("customerId");

        // Verify the customerId is not null or empty
        assertThat(customerId).isNotEmpty();
        System.out.println("Customer ID: " + customerId);
    }

    @Test
    @Order(2)
    public void testLoanRequest() {
        // Create a loan request
        LoanRequest loanRequest = LoanRequest.builder()
                .customerNumber(TestConfig.TEST_CUSTOMER_NUMBER)
                .amount(TestConfig.TEST_LOAN_AMOUNT)
                .build();

        // Send the request and validate the response
        loanId = given()
                .spec(lmsRequestSpec)
                .body(loanRequest)
                .when()
                .post("/loans")
                .then()
                .statusCode(201)
                .body("loanId", notNullValue())
                .body("status", equalTo("PENDING"))
                .extract()
                .path("loanId");

        // Verify the loanId is not null or empty
        assertThat(loanId).isNotEmpty();
        System.out.println("Loan ID: " + loanId);
    }

    @Test
    @Order(3)
    public void testInitiateScoreQuery() {
        // Create a score request
        ScoreRequest scoreRequest = ScoreRequest.builder()
                .customerNumber(TestConfig.TEST_CUSTOMER_NUMBER)
                .build();

        // Send the request and validate the response
        String token = given()
                .spec(lmsRequestSpec)
                .body(scoreRequest)
                .when()
                .post("/scores")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .path("token");

        // Verify the token is not null or empty
        assertThat(token).isNotEmpty();
        System.out.println("Score Token: " + token);
    }

    @Test
    @Order(4)
    public void testQueryLoanStatus() {
        // Wait a bit for scoring to complete
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Send the request and validate the response
        LoanResponse loanResponse = given()
                .spec(lmsRequestSpec)
                .when()
                .get("/loans/" + loanId)
                .then()
                .statusCode(200)
                .body("loanId", equalTo(loanId))
                .body("status", notNullValue())
                .body("amount", equalTo(Float.valueOf(TestConfig.TEST_LOAN_AMOUNT + "f")))
                .extract()
                .as(LoanResponse.class);

        // Print the loan details
        System.out.println("Loan Status: " + loanResponse.getStatus());
        System.out.println("Loan Score: " + loanResponse.getScore());
        System.out.println("Loan Limit: " + loanResponse.getLimit());
    }

    @Test
    @Order(5)
    public void testLoanNotFound() {
        // Send a request for a non-existent loan ID
        given()
                .spec(lmsRequestSpec)
                .when()
                .get("/loans/non-existent-id")
                .then()
                .statusCode(404)
                .body("error", equalTo("Loan not found"));
    }
}

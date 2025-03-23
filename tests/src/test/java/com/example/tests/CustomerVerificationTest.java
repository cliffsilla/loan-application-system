package com.example.tests;

import com.example.tests.config.TestConfig;
import com.example.tests.models.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class CustomerVerificationTest extends BaseApiTest {

    private static Stream<String> customerIds() {
        return Stream.of(TestConfig.TEST_CUSTOMER_IDS);
    }

    @ParameterizedTest(name = "Verify customer ID: {0}")
    @MethodSource("customerIds")
    @DisplayName("Verify all customer IDs can be subscribed")
    public void testCustomerSubscription(String customerId) {
        // Create a customer subscription request
        Customer customer = Customer.builder()
                .customerNumber(customerId)
                .build();

        // Send the request and validate the response
        String responseCustomerId = given()
                .spec(lmsRequestSpec)
                .body(customer)
                .when()
                .post("/subscriptions")
                .then()
                .statusCode(200) // Expect 200 for success or 400 if already subscribed
                .extract()
                .path("customerId");

        System.out.println("Customer ID " + customerId + " subscription result: " + 
                          (responseCustomerId != null ? "Success with ID: " + responseCustomerId : "Already subscribed"));
    }

    @ParameterizedTest(name = "Verify loan request for customer: {0}")
    @MethodSource("customerIds")
    @DisplayName("Verify all customer IDs can request loans")
    public void testLoanRequest(String customerId) {
        // First make sure the customer is subscribed
        try {
            Customer customer = Customer.builder()
                    .customerNumber(customerId)
                    .build();

            given()
                    .spec(lmsRequestSpec)
                    .body(customer)
                    .when()
                    .post("/subscriptions");
        } catch (Exception e) {
            // Ignore if already subscribed
        }

        // Create a loan request
        given()
                .spec(lmsRequestSpec)
                .body(String.format("{\"customerNumber\":\"%s\",\"amount\":1000.00}", customerId))
                .when()
                .post("/loans")
                .then()
                .statusCode(201)
                .body("loanId", notNullValue())
                .body("status", notNullValue());

        System.out.println("Successfully created loan request for customer ID: " + customerId);
    }

    @ParameterizedTest(name = "Verify transaction data for customer: {0}")
    @MethodSource("customerIds")
    @DisplayName("Verify transaction data can be retrieved for all customers")
    public void testTransactionData(String customerId) {
        // Send the request to get transaction data
        given()
                .spec(middlewareRequestSpec)
                .when()
                .get("/transactions/" + customerId)
                .then()
                .statusCode(200);

        System.out.println("Successfully retrieved transaction data for customer ID: " + customerId);
    }
}

package com.example.tests;

import com.example.tests.config.TestConfig;
import com.example.tests.models.Transaction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class MiddlewareApiTest extends BaseApiTest {

    @Test
    public void testRetrieveTransactionData() {
        // Send the request and validate the response
        List<Transaction> transactions = given()
                .spec(middlewareRequestSpec)
                .when()
                .get("/transactions/" + TestConfig.TEST_CUSTOMER_NUMBER)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath()
                .getList(".", Transaction.class);

        // Verify that we received transaction data
        assertThat(transactions).isNotNull();
        // There might be no transactions for a test customer, so we're not asserting size
        
        // Print transaction details if available
        if (!transactions.isEmpty()) {
            System.out.println("Retrieved " + transactions.size() + " transactions");
            transactions.forEach(transaction -> {
                System.out.println("Account: " + transaction.getAccountNumber() + 
                                 ", Date: " + transaction.getTransactionDate() + 
                                 ", Amount: " + transaction.getAmount());
            });
        } else {
            System.out.println("No transactions found for customer: " + TestConfig.TEST_CUSTOMER_NUMBER);
        }
    }

    @Test
    public void testRetrieveTransactionDataForInvalidCustomer() {
        // Send a request for a non-existent customer
        given()
                .spec(middlewareRequestSpec)
                .when()
                .get("/transactions/non-existent-customer")
                .then()
                .statusCode(404); // Expecting a 404 Not Found response
    }
}

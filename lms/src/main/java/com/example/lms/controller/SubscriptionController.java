package com.example.lms.controller;

import com.example.lms.dto.ErrorResponse;
import com.example.lms.dto.SubscriptionRequest;
import com.example.lms.dto.SubscriptionResponse;
import com.example.lms.entity.Customer;
import com.example.lms.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.xml.bind.JAXBException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscription", description = "Subscription API")
public class SubscriptionController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Subscribe a customer", description = "Subscribes a customer to the LMS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer subscribed successfully",
                    content = @Content(schema = @Schema(implementation = SubscriptionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> subscribeCustomer(@Valid @RequestBody SubscriptionRequest request) {
        try {
            Customer customer = customerService.subscribeCustomer(request.getCustomerNumber());
            return ResponseEntity.ok(new SubscriptionResponse(customer.getCustomerId()));
        } catch (JAXBException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Error processing CBS response"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
}

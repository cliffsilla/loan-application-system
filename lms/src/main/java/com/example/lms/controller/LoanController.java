package com.example.lms.controller;

import com.example.lms.dto.ErrorResponse;
import com.example.lms.dto.LoanRequest;
import com.example.lms.dto.LoanResponse;
import com.example.lms.dto.LoanStatusResponse;
import com.example.lms.entity.Loan;
import com.example.lms.service.LoanService;
import com.example.lms.service.ScoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "Loan API")
public class LoanController {

    private final LoanService loanService;
    private final ScoringService scoringService;

    @PostMapping
    @Operation(summary = "Apply for a loan", description = "Submits a loan application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Loan application created successfully",
                    content = @Content(schema = @Schema(implementation = LoanResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> applyForLoan(@Valid @RequestBody LoanRequest request) {
        try {
            Loan loan = loanService.createLoanApplication(request.getCustomerNumber(), request.getAmount());
            scoringService.scoreLoan(loan);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new LoanResponse(loan.getLoanId(), loan.getStatus()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{loanId}")
    @Operation(summary = "Get loan status", description = "Retrieves the status of a loan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan status retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LoanStatusResponse.class))),
            @ApiResponse(responseCode = "404", description = "Loan not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getLoanStatus(@PathVariable UUID loanId) {
        if (loanService.getLoanStatus(loanId).isPresent()) {
            Loan loan = loanService.getLoanStatus(loanId).get();
            LoanStatusResponse response = new LoanStatusResponse(
                loan.getLoanId(),
                loan.getStatus(),
                loan.getAmount(),
                loan.getScore(),
                loan.getLimit(),
                loan.getRejectionReason()
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Loan not found"));
        }
    }
}

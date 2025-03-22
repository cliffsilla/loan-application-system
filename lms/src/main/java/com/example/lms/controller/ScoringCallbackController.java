package com.example.lms.controller;

import com.example.lms.dto.ErrorResponse;
import com.example.lms.dto.ScoringCallbackRequest;
import com.example.lms.dto.ScoringCallbackResponse;
import com.example.lms.entity.Loan;
import com.example.lms.service.ScoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scoring/callback")
@RequiredArgsConstructor
@Tag(name = "Scoring Callback", description = "Scoring Callback API")
public class ScoringCallbackController {

    private final ScoringService scoringService;

    @PostMapping
    @Operation(summary = "Process scoring callback", description = "Processes a callback from the Scoring Engine with score results")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Callback processed successfully",
                    content = @Content(schema = @Schema(implementation = ScoringCallbackResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> processCallback(@Valid @RequestBody ScoringCallbackRequest request) {
        try {
            Loan loan = scoringService.processScoreCallback(
                    request.getToken(),
                    request.getScore(),
                    request.getLimit()
            );
            return ResponseEntity.ok(new ScoringCallbackResponse(loan.getLoanId(), loan.getStatus()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}

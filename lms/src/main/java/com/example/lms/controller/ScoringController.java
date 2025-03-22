package com.example.lms.controller;

import com.example.lms.dto.ErrorResponse;
import com.example.lms.dto.ScoreQueryRequest;
import com.example.lms.dto.ScoreQueryResponse;
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
@RequestMapping("/scores")
@RequiredArgsConstructor
@Tag(name = "Scoring", description = "Scoring API")
public class ScoringController {

    private final ScoringService scoringService;

    @PostMapping
    @Operation(summary = "Initiate score query", description = "Initiates the process of querying the score from the Scoring Engine")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Score query initiated successfully",
                    content = @Content(schema = @Schema(implementation = ScoreQueryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> initiateScoreQuery(@Valid @RequestBody ScoreQueryRequest request) {
        try {
            String token = scoringService.initiateScoreQuery(request.getCustomerNumber());
            return ResponseEntity.ok(new ScoreQueryResponse(token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}

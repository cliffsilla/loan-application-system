package com.example.lms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoringCallbackRequest {
    
    @NotBlank(message = "Token is required")
    @Schema(description = "Token from the initial score query", example = "550e8400-e29b-41d4-a716-446655440000")
    private String token;
    
    @NotNull(message = "Score is required")
    @Schema(description = "Calculated credit score", example = "750.0")
    private Double score;
    
    @NotNull(message = "Limit is required")
    @Schema(description = "Calculated credit limit", example = "10000.0")
    private Double limit;
}

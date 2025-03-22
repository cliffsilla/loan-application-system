package com.example.lms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoringCallbackResponse {
    
    @Schema(description = "Loan ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID loanId;
    
    @Schema(description = "Loan status", example = "APPROVED")
    private String status;
}

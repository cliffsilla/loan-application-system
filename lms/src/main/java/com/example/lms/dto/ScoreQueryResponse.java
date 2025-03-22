package com.example.lms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreQueryResponse {
    
    @Schema(description = "Token to be used to retrieve the score", example = "550e8400-e29b-41d4-a716-446655440000")
    private String token;
}

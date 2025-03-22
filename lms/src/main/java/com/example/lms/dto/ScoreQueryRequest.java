package com.example.lms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreQueryRequest {
    
    @NotBlank(message = "Customer number is required")
    @Schema(description = "Customer number", example = "C12345")
    private String customerNumber;
}

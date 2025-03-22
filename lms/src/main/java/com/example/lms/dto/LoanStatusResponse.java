package com.example.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanStatusResponse {
    private UUID loanId;
    private String status;
    private Double amount;
    private Double score;
    private Double limit;
    private String rejectionReason;
}

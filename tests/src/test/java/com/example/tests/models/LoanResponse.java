package com.example.tests.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {
    private String loanId;
    private String status;
    private Double amount;
    private Integer score;
    private Double limit;
    private String rejectionReason;
}

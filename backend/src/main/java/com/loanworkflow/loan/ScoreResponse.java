package com.loanworkflow.loan;

import com.loanworkflow.scoring.RiskLevel;

/** Result of scoring: the loan may already be REJECTED when the risk is HIGH. */
public record ScoreResponse(String id, LoanStatus status, int score, RiskLevel risk) {
}

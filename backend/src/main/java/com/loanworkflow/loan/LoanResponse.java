package com.loanworkflow.loan;

import java.math.BigDecimal;

/** What the API returns, so domain objects are never exposed directly. */
public record LoanResponse(String id, LoanStatus status, BigDecimal amount) {
}

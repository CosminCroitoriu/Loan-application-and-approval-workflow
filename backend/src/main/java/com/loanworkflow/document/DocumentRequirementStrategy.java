package com.loanworkflow.document;

import java.math.BigDecimal;
import java.util.Set;

import com.loanworkflow.loan.LoanType;

/**
 * Strategy interface for determining the required documents for a loan.
 * Different loan types (personal, auto, mortgage, business) use different strategies.
 */
public interface DocumentRequirementStrategy {

    boolean supports(LoanType loanType);

    Set<DocumentType> requiredDocuments(BigDecimal amount);
}

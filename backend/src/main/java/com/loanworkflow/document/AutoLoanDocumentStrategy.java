package com.loanworkflow.document;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

import com.loanworkflow.loan.LoanType;

import org.springframework.stereotype.Component;

/**
 * Document requirements strategy for vehicle financing loans.
 * Requires identity verification, proof of stable income, and vehicle title/collateral pledge.
 */
@Component
public class AutoLoanDocumentStrategy implements DocumentRequirementStrategy {

    @Override
    public boolean supports(LoanType loanType) {
        return loanType == LoanType.AUTO;
    }

    @Override
    public Set<DocumentType> requiredDocuments(BigDecimal amount) {
        return EnumSet.of(
                DocumentType.IDENTITY_CARD,
                DocumentType.PROOF_OF_INCOME,
                DocumentType.COLLATERAL_PROOF
        );
    }
}

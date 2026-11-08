package com.loanworkflow.document;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

import com.loanworkflow.loan.LoanType;

import org.springframework.stereotype.Component;

/**
 * Document requirements strategy for corporate and commercial business loans.
 * Requires representative identity, corporate financial statements, and tax clearance certificates.
 * For amounts exceeding 100,000 EUR, corporate collateral proof is required.
 */
@Component
public class BusinessLoanDocumentStrategy implements DocumentRequirementStrategy {

    public static final BigDecimal HIGH_VALUE_BUSINESS_THRESHOLD = new BigDecimal("100000");

    @Override
    public boolean supports(LoanType loanType) {
        return loanType == LoanType.BUSINESS;
    }

    @Override
    public Set<DocumentType> requiredDocuments(BigDecimal amount) {
        Set<DocumentType> required = EnumSet.of(
                DocumentType.IDENTITY_CARD,
                DocumentType.FINANCIAL_STATEMENT,
                DocumentType.TAX_RECORD
        );
        if (amount != null && amount.compareTo(HIGH_VALUE_BUSINESS_THRESHOLD) > 0) {
            required.add(DocumentType.COLLATERAL_PROOF);
        }
        return required;
    }
}

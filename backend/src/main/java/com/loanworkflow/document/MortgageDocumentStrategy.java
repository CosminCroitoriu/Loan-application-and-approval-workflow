package com.loanworkflow.document;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

import com.loanworkflow.loan.LoanType;

import org.springframework.stereotype.Component;

/**
 * Document requirements strategy for real estate mortgage loans.
 * Requires borrower identity, income documentation, property deed/cadastre, and tax certificate.
 */
@Component
public class MortgageDocumentStrategy implements DocumentRequirementStrategy {

    @Override
    public boolean supports(LoanType loanType) {
        return loanType == LoanType.MORTGAGE;
    }

    @Override
    public Set<DocumentType> requiredDocuments(BigDecimal amount) {
        return EnumSet.of(
                DocumentType.IDENTITY_CARD,
                DocumentType.PROOF_OF_INCOME,
                DocumentType.PROPERTY_DEED,
                DocumentType.TAX_RECORD
        );
    }
}

package com.loanworkflow.document;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

import com.loanworkflow.loan.LoanType;

import org.springframework.stereotype.Component;

/**
 * Document requirements strategy for personal unsecured loans.
 * Personal loans require identity proof and income proof.
 * For amounts exceeding 50,000 EUR, collateral proof is also mandated.
 */
@Component
public class PersonalLoanDocumentStrategy implements DocumentRequirementStrategy {

    public static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("50000");

    @Override
    public boolean supports(LoanType loanType) {
        return loanType == LoanType.PERSONAL;
    }

    @Override
    public Set<DocumentType> requiredDocuments(BigDecimal amount) {
        Set<DocumentType> required = EnumSet.of(DocumentType.IDENTITY_CARD, DocumentType.PROOF_OF_INCOME);
        if (amount != null && amount.compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            required.add(DocumentType.COLLATERAL_PROOF);
        }
        return required;
    }
}

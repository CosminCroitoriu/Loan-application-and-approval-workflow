package com.loanworkflow.document;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.loanworkflow.loan.LoanType;

import org.springframework.stereotype.Service;

/**
 * Registry resolving the appropriate DocumentRequirementStrategy for a given LoanType.
 */
@Service
public class DocumentRequirementRegistry {

    private final List<DocumentRequirementStrategy> strategies;

    public DocumentRequirementRegistry(List<DocumentRequirementStrategy> strategies) {
        this.strategies = strategies;
    }

    public DocumentRequirementStrategy findStrategy(LoanType loanType) {
        if (loanType == null) {
            loanType = LoanType.PERSONAL;
        }
        for (DocumentRequirementStrategy strategy : strategies) {
            if (strategy.supports(loanType)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("No document requirement strategy configured for loan type: " + loanType);
    }

    public Set<DocumentType> requiredDocumentsFor(LoanType loanType, BigDecimal amount) {
        return findStrategy(loanType).requiredDocuments(amount);
    }
}

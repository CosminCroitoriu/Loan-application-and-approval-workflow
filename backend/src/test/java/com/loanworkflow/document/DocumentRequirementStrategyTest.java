package com.loanworkflow.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.loanworkflow.loan.LoanType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DocumentRequirementStrategyTest {

    private DocumentRequirementRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DocumentRequirementRegistry(List.of(
                new PersonalLoanDocumentStrategy(),
                new AutoLoanDocumentStrategy(),
                new MortgageDocumentStrategy(),
                new BusinessLoanDocumentStrategy()
        ));
    }

    @Test
    void personalLoanUnderThresholdRequiresIdentityAndIncome() {
        Set<DocumentType> required = registry.requiredDocumentsFor(LoanType.PERSONAL, new BigDecimal("25000"));
        assertEquals(Set.of(DocumentType.IDENTITY_CARD, DocumentType.PROOF_OF_INCOME), required);
    }

    @Test
    void personalLoanOverThresholdRequiresAdditionalCollateralProof() {
        Set<DocumentType> required = registry.requiredDocumentsFor(LoanType.PERSONAL, new BigDecimal("55000"));
        assertEquals(
                Set.of(DocumentType.IDENTITY_CARD, DocumentType.PROOF_OF_INCOME, DocumentType.COLLATERAL_PROOF),
                required
        );
    }

    @Test
    void autoLoanRequiresIdentityIncomeAndCollateral() {
        Set<DocumentType> required = registry.requiredDocumentsFor(LoanType.AUTO, new BigDecimal("30000"));
        assertEquals(
                Set.of(DocumentType.IDENTITY_CARD, DocumentType.PROOF_OF_INCOME, DocumentType.COLLATERAL_PROOF),
                required
        );
    }

    @Test
    void mortgageLoanRequiresIdentityIncomePropertyDeedAndTaxRecord() {
        Set<DocumentType> required = registry.requiredDocumentsFor(LoanType.MORTGAGE, new BigDecimal("120000"));
        assertEquals(
                Set.of(DocumentType.IDENTITY_CARD, DocumentType.PROOF_OF_INCOME,
                        DocumentType.PROPERTY_DEED, DocumentType.TAX_RECORD),
                required
        );
    }

    @Test
    void businessLoanRequiresFinancialStatementsAndTaxRecord() {
        Set<DocumentType> standard = registry.requiredDocumentsFor(LoanType.BUSINESS, new BigDecimal("50000"));
        assertEquals(
                Set.of(DocumentType.IDENTITY_CARD, DocumentType.FINANCIAL_STATEMENT, DocumentType.TAX_RECORD),
                standard
        );

        Set<DocumentType> large = registry.requiredDocumentsFor(LoanType.BUSINESS, new BigDecimal("150000"));
        assertTrue(large.contains(DocumentType.COLLATERAL_PROOF));
        assertEquals(4, large.size());
    }

    @Test
    void strategySupportsMatchesLoanTypes() {
        PersonalLoanDocumentStrategy personal = new PersonalLoanDocumentStrategy();
        assertTrue(personal.supports(LoanType.PERSONAL));
        assertFalse(personal.supports(LoanType.MORTGAGE));
    }
}

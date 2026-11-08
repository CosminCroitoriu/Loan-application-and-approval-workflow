package com.loanworkflow.loan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import com.loanworkflow.approval.Role;
import com.loanworkflow.approval.User;
import com.loanworkflow.document.AutoLoanDocumentStrategy;
import com.loanworkflow.document.BusinessLoanDocumentStrategy;
import com.loanworkflow.document.DocumentCheckService;
import com.loanworkflow.document.DocumentRequirementRegistry;
import com.loanworkflow.document.DocumentStatus;
import com.loanworkflow.document.DocumentType;
import com.loanworkflow.document.IncompleteDocumentsException;
import com.loanworkflow.document.MortgageDocumentStrategy;
import com.loanworkflow.document.PersonalLoanDocumentStrategy;
import com.loanworkflow.persistence.entity.DocumentEntity;
import com.loanworkflow.scoring.RuleBasedScoringStrategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoanServiceDocumentCheckTest {

    private final User applicant = new User("carol", Role.APPLICANT);
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-08T10:00:00Z"), ZoneOffset.UTC);
    private DocumentCheckService checkService;

    @BeforeEach
    void setUp() {
        DocumentRequirementRegistry registry = new DocumentRequirementRegistry(List.of(
                new PersonalLoanDocumentStrategy(),
                new AutoLoanDocumentStrategy(),
                new MortgageDocumentStrategy(),
                new BusinessLoanDocumentStrategy()
        ));
        checkService = new DocumentCheckService(registry, clock);
    }

    private DocumentEntity verifiedDoc(DocumentType type) {
        DocumentEntity doc = new DocumentEntity(type, type.name().toLowerCase() + ".pdf");
        doc.setStatus(DocumentStatus.VERIFIED);
        doc.setVerifiedBy("officer_bob");
        doc.setVerifiedAt(clock.instant());
        return doc;
    }

    @Test
    void movesToDocumentsCheckedWhenValidDocumentsProvided() {
        LoanService service = new LoanService(applicant, new BigDecimal("15000"), new RuleBasedScoringStrategy(), clock);
        List<DocumentEntity> docs = List.of(
                verifiedDoc(DocumentType.IDENTITY_CARD),
                verifiedDoc(DocumentType.PROOF_OF_INCOME)
        );

        service.checkDocuments(checkService, LoanType.PERSONAL, docs);
        assertEquals(LoanStatus.DOCUMENTS_CHECKED, service.loan().status());
    }

    @Test
    void throwsIncompleteDocumentsExceptionAndStaysSubmittedWhenDocsMissing() {
        LoanService service = new LoanService(applicant, new BigDecimal("15000"), new RuleBasedScoringStrategy(), clock);
        // Only ID card provided, missing income proof
        List<DocumentEntity> docs = List.of(verifiedDoc(DocumentType.IDENTITY_CARD));

        assertThrows(IncompleteDocumentsException.class, () ->
                service.checkDocuments(checkService, LoanType.PERSONAL, docs));

        assertEquals(LoanStatus.SUBMITTED, service.loan().status());
    }
}

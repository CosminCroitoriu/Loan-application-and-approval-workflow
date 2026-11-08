package com.loanworkflow.document;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import com.loanworkflow.loan.LoanType;
import com.loanworkflow.persistence.entity.DocumentEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DocumentCheckServiceTest {

    private static final Instant EVALUATION_TIME = Instant.parse("2026-11-08T12:00:00Z");
    private Clock clock;
    private DocumentRequirementRegistry registry;
    private DocumentCheckService checkService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(EVALUATION_TIME, ZoneOffset.UTC);
        registry = new DocumentRequirementRegistry(List.of(
                new PersonalLoanDocumentStrategy(),
                new AutoLoanDocumentStrategy(),
                new MortgageDocumentStrategy(),
                new BusinessLoanDocumentStrategy()
        ));
        checkService = new DocumentCheckService(registry, clock);
    }

    private DocumentEntity verifiedDoc(DocumentType type, Instant expiryDate) {
        DocumentEntity doc = new DocumentEntity(type, type.name().toLowerCase() + ".pdf");
        doc.setStatus(DocumentStatus.VERIFIED);
        doc.setVerifiedBy("officer_alice");
        doc.setVerifiedAt(EVALUATION_TIME.minusSeconds(3600));
        doc.setExpiryDate(expiryDate);
        return doc;
    }

    @Test
    void happyPathPersonalLoanWithVerifiedDocsSucceeds() {
        List<DocumentEntity> docs = List.of(
                verifiedDoc(DocumentType.IDENTITY_CARD, EVALUATION_TIME.plusSeconds(86400 * 30)),
                verifiedDoc(DocumentType.PROOF_OF_INCOME, null)
        );

        assertDoesNotThrow(() ->
                checkService.verifyDocuments(LoanType.PERSONAL, new BigDecimal("10000"), docs));
    }

    @Test
    void missingRequiredDocumentThrowsIncompleteDocumentsException() {
        // Missing PROOF_OF_INCOME
        List<DocumentEntity> docs = List.of(
                verifiedDoc(DocumentType.IDENTITY_CARD, EVALUATION_TIME.plusSeconds(86400 * 30))
        );

        IncompleteDocumentsException ex = assertThrows(IncompleteDocumentsException.class, () ->
                checkService.verifyDocuments(LoanType.PERSONAL, new BigDecimal("10000"), docs));

        assertTrue(ex.getMissingOrInvalidDocuments().stream()
                .anyMatch(issue -> issue.contains("Missing required document: PROOF_OF_INCOME")));
    }

    @Test
    void unverifiedDocumentThrowsIncompleteDocumentsException() {
        DocumentEntity idCard = verifiedDoc(DocumentType.IDENTITY_CARD, EVALUATION_TIME.plusSeconds(86400 * 30));
        DocumentEntity pendingIncome = new DocumentEntity(DocumentType.PROOF_OF_INCOME, "income.pdf");
        pendingIncome.setStatus(DocumentStatus.PENDING);

        IncompleteDocumentsException ex = assertThrows(IncompleteDocumentsException.class, () ->
                checkService.verifyDocuments(LoanType.PERSONAL, new BigDecimal("10000"), List.of(idCard, pendingIncome)));

        assertTrue(ex.getMissingOrInvalidDocuments().stream()
                .anyMatch(issue -> issue.contains("present but not verified")));
    }

    @Test
    void expiredDocumentIsMarkedExpiredAndThrowsException() {
        // Document expired 1 hour before evaluation time
        Instant expiredAt = EVALUATION_TIME.minusSeconds(3600);
        DocumentEntity expiredId = verifiedDoc(DocumentType.IDENTITY_CARD, expiredAt);
        DocumentEntity validIncome = verifiedDoc(DocumentType.PROOF_OF_INCOME, null);

        IncompleteDocumentsException ex = assertThrows(IncompleteDocumentsException.class, () ->
                checkService.verifyDocuments(LoanType.PERSONAL, new BigDecimal("10000"), List.of(expiredId, validIncome)));

        assertEquals(DocumentStatus.EXPIRED, expiredId.getStatus());
        assertTrue(ex.getMessage().contains("requirement(s) not satisfied"));
    }

    // --- Time-dependent verification with Clock across different dates ---

    @ParameterizedTest
    @ValueSource(strings = {
            "2026-12-31T23:59:59Z", // End of year
            "2027-01-01T00:00:01Z", // Start of next year
            "2028-02-29T12:00:00Z"  // Leap day in 2028
    })
    void clockCorrectlyDetectsValidVsExpiredDocuments(String testInstantIso) {
        Instant testNow = Instant.parse(testInstantIso);
        Clock customClock = Clock.fixed(testNow, ZoneOffset.UTC);
        DocumentCheckService serviceWithCustomClock = new DocumentCheckService(registry, customClock);

        // Document that expires 10 seconds AFTER testNow -> VALID
        DocumentEntity validDoc = verifiedDoc(DocumentType.IDENTITY_CARD, testNow.plusSeconds(10));
        DocumentEntity validIncome = verifiedDoc(DocumentType.PROOF_OF_INCOME, null);

        assertDoesNotThrow(() ->
                serviceWithCustomClock.verifyDocuments(
                        LoanType.PERSONAL,
                        new BigDecimal("10000"),
                        List.of(validDoc, validIncome)
                ));

        // Document that expired 10 seconds BEFORE testNow -> EXPIRED
        DocumentEntity expiredDoc = verifiedDoc(DocumentType.IDENTITY_CARD, testNow.minusSeconds(10));
        assertThrows(IncompleteDocumentsException.class, () ->
                serviceWithCustomClock.verifyDocuments(
                        LoanType.PERSONAL,
                        new BigDecimal("10000"),
                        List.of(expiredDoc, validIncome)
                ));
        assertEquals(DocumentStatus.EXPIRED, expiredDoc.getStatus());
    }
}

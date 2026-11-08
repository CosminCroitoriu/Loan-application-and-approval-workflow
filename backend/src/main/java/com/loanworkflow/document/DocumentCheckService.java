package com.loanworkflow.document;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.loanworkflow.loan.LoanType;
import com.loanworkflow.persistence.entity.DocumentEntity;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Validates whether the submitted documents for a loan application satisfy
 * all business requirements and have not expired as of the configured Clock time.
 */
@Service
public class DocumentCheckService {

    private final DocumentRequirementRegistry registry;
    private final Clock clock;

    @Autowired
    public DocumentCheckService(
            DocumentRequirementRegistry registry,
            ObjectProvider<Clock> clockProvider
    ) {
        this.registry = registry;
        this.clock = clockProvider != null
                ? clockProvider.getIfAvailable(Clock::systemUTC)
                : Clock.systemUTC();
    }

    public DocumentCheckService(DocumentRequirementRegistry registry, Clock clock) {
        this.registry = registry;
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    /**
     * Verifies that all required documents for the loan type and amount are present,
     * fully verified by an officer, and not expired according to the clock.
     *
     * @param loanType category of loan
     * @param amount requested principal amount
     * @param submittedDocuments documents attached to the application
     * @throws IncompleteDocumentsException when requirements are not satisfied
     */
    public void verifyDocuments(
            LoanType loanType,
            BigDecimal amount,
            List<DocumentEntity> submittedDocuments
    ) {
        Set<DocumentType> requiredTypes = registry.requiredDocumentsFor(loanType, amount);
        Instant now = clock.instant();

        List<DocumentEntity> docs = submittedDocuments != null ? submittedDocuments : List.of();

        // 1. Update status to EXPIRED for any document that past its expiry date
        for (DocumentEntity doc : docs) {
            if (doc.getExpiryDate() != null && doc.getExpiryDate().isBefore(now)) {
                doc.setStatus(DocumentStatus.EXPIRED);
            }
        }

        // 2. Group verified and non-expired documents by type
        Map<DocumentType, List<DocumentEntity>> verifiedByType = docs.stream()
                .filter(d -> d.getStatus() == DocumentStatus.VERIFIED)
                .collect(Collectors.groupingBy(DocumentEntity::getDocumentType));

        // 3. Find missing or non-verified requirements
        List<String> issues = new ArrayList<>();
        for (DocumentType required : requiredTypes) {
            List<DocumentEntity> matching = verifiedByType.get(required);
            if (matching == null || matching.isEmpty()) {
                // Check if document was submitted but has a non-verified status
                List<DocumentEntity> unverified = docs.stream()
                        .filter(d -> d.getDocumentType() == required)
                        .toList();

                if (unverified.isEmpty()) {
                    issues.add("Missing required document: " + required.name());
                } else {
                    String statuses = unverified.stream()
                            .map(d -> d.getStatus().name())
                            .collect(Collectors.joining(", "));
                    issues.add("Document " + required.name() + " is present but not verified (status: "
                            + statuses + ")");
                }
            }
        }

        if (!issues.isEmpty()) {
            throw new IncompleteDocumentsException(
                    "Document check failed: " + issues.size() + " requirement(s) not satisfied",
                    issues
            );
        }
    }
}

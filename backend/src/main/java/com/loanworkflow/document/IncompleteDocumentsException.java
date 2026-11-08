package com.loanworkflow.document;

import java.util.Collections;
import java.util.List;

/**
 * Thrown when attempting to advance a loan to DOCUMENTS_CHECKED without
 * having all required documents submitted and verified.
 */
public class IncompleteDocumentsException extends RuntimeException {

    private final List<String> missingOrInvalidDocuments;

    public IncompleteDocumentsException(String message, List<String> missingOrInvalidDocuments) {
        super(message);
        this.missingOrInvalidDocuments = missingOrInvalidDocuments != null
                ? List.copyOf(missingOrInvalidDocuments)
                : Collections.emptyList();
    }

    public IncompleteDocumentsException(String message) {
        this(message, Collections.emptyList());
    }

    public List<String> getMissingOrInvalidDocuments() {
        return missingOrInvalidDocuments;
    }
}

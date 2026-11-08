package com.loanworkflow.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.loanworkflow.document.DocumentStatus;
import com.loanworkflow.document.DocumentType;
import com.loanworkflow.loan.LoanStatus;
import com.loanworkflow.loan.LoanType;
import com.loanworkflow.persistence.entity.DocumentEntity;
import com.loanworkflow.persistence.entity.LoanApplicationEntity;
import com.loanworkflow.persistence.entity.StatusChangeEntity;
import com.loanworkflow.persistence.repository.LoanApplicationRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class LoanApplicationPersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LoanApplicationRepository loanRepository;

    @Test
    void savesAndRetrievesLoanWithCascadeDocumentsAndHistory() {
        LoanApplicationEntity loan = new LoanApplicationEntity("carol", new BigDecimal("15000.00"), LoanType.AUTO);

        DocumentEntity idCard = new DocumentEntity(DocumentType.IDENTITY_CARD, "id_card.pdf");
        idCard.setStatus(DocumentStatus.VERIFIED);
        idCard.setVerifiedBy("officer_bob");
        idCard.setVerifiedAt(Instant.now());
        loan.addDocument(idCard);

        StatusChangeEntity change1 = new StatusChangeEntity(
                null, LoanStatus.SUBMITTED, Instant.now(), "Application created");
        StatusChangeEntity change2 = new StatusChangeEntity(
                LoanStatus.SUBMITTED, LoanStatus.DOCUMENTS_CHECKED, Instant.now(), "Documents verified by officer");
        loan.addStatusChange(change1);
        loan.addStatusChange(change2);

        LoanApplicationEntity saved = loanRepository.save(loan);
        entityManager.flush();
        entityManager.clear();

        assertNotNull(saved.getId());
        Optional<LoanApplicationEntity> retrieved = loanRepository.findById(saved.getId());
        assertTrue(retrieved.isPresent());

        LoanApplicationEntity loaded = retrieved.get();
        assertEquals("carol", loaded.getApplicantUsername());
        assertEquals(new BigDecimal("15000.0000"), loaded.getAmount());
        assertEquals(LoanType.AUTO, loaded.getLoanType());
        assertEquals(1, loaded.getDocuments().size());
        assertEquals(DocumentType.IDENTITY_CARD, loaded.getDocuments().get(0).getDocumentType());
        assertEquals(2, loaded.getStatusHistory().size());
    }

    @Test
    void findsLoansByStatusAndApplicant() {
        LoanApplicationEntity loan1 = new LoanApplicationEntity("alice", new BigDecimal("5000"), LoanType.PERSONAL);
        loan1.setStatus(LoanStatus.SUBMITTED);
        LoanApplicationEntity loan2 = new LoanApplicationEntity("bob", new BigDecimal("50000"), LoanType.MORTGAGE);
        loan2.setStatus(LoanStatus.SCORED);
        LoanApplicationEntity loan3 = new LoanApplicationEntity("alice", new BigDecimal("20000"), LoanType.AUTO);
        loan3.setStatus(LoanStatus.APPROVED);

        loanRepository.saveAll(List.of(loan1, loan2, loan3));
        entityManager.flush();
        entityManager.clear();

        List<LoanApplicationEntity> submittedLoans = loanRepository.findByStatus(LoanStatus.SUBMITTED);
        assertEquals(1, submittedLoans.size());
        assertEquals("alice", submittedLoans.get(0).getApplicantUsername());

        List<LoanApplicationEntity> aliceLoans = loanRepository.findByApplicantUsername("alice");
        assertEquals(2, aliceLoans.size());

        List<LoanApplicationEntity> mortgageLoans = loanRepository.findByLoanType(LoanType.MORTGAGE);
        assertEquals(1, mortgageLoans.size());
    }
}

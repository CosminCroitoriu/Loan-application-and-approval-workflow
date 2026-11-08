package com.loanworkflow.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.loanworkflow.loan.LoanType;
import com.loanworkflow.persistence.entity.ApprovalRecordEntity;
import com.loanworkflow.persistence.entity.LoanApplicationEntity;
import com.loanworkflow.persistence.repository.ApprovalRecordRepository;
import com.loanworkflow.persistence.repository.LoanApplicationRepository;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ApprovalRecordPersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LoanApplicationRepository loanRepository;

    @Autowired
    private ApprovalRecordRepository approvalRepository;

    @Test
    void savesTwoApprovalsInOrderForFourEyesPrinciple() {
        LoanApplicationEntity loan = new LoanApplicationEntity("david", new BigDecimal("75000"), LoanType.MORTGAGE);
        LoanApplicationEntity savedLoan = loanRepository.save(loan);

        ApprovalRecordEntity firstApproval = new ApprovalRecordEntity("officer_anna", 1, Instant.now());
        firstApproval.setLoanApplication(savedLoan);
        approvalRepository.save(firstApproval);

        ApprovalRecordEntity secondApproval = new ApprovalRecordEntity("officer_ben", 2, Instant.now().plusSeconds(60));
        secondApproval.setLoanApplication(savedLoan);
        approvalRepository.save(secondApproval);

        entityManager.flush();
        entityManager.clear();

        List<ApprovalRecordEntity> approvals =
                approvalRepository.findByLoanApplicationIdOrderBySequenceNumberAsc(savedLoan.getId());
        assertEquals(2, approvals.size());
        assertEquals("officer_anna", approvals.get(0).getApproverName());
        assertEquals(1, approvals.get(0).getSequenceNumber());
        assertEquals("officer_ben", approvals.get(1).getApproverName());
        assertEquals(2, approvals.get(1).getSequenceNumber());

        assertTrue(approvalRepository.existsByLoanApplicationIdAndApproverName(savedLoan.getId(), "officer_anna"));
        assertEquals(2, approvalRepository.countByLoanApplicationId(savedLoan.getId()));
    }

    @Test
    void rejectsDuplicateApprovalFromSameUserOnSameLoan() {
        LoanApplicationEntity loan = new LoanApplicationEntity("david", new BigDecimal("75000"), LoanType.MORTGAGE);
        LoanApplicationEntity savedLoan = loanRepository.save(loan);

        ApprovalRecordEntity firstApproval = new ApprovalRecordEntity("officer_anna", 1, Instant.now());
        firstApproval.setLoanApplication(savedLoan);
        approvalRepository.save(firstApproval);
        entityManager.flush();

        ApprovalRecordEntity duplicateApproval = new ApprovalRecordEntity("officer_anna", 2, Instant.now());
        duplicateApproval.setLoanApplication(savedLoan);
        approvalRepository.save(duplicateApproval);

        assertThrows(ConstraintViolationException.class, () -> entityManager.flush());
    }
}

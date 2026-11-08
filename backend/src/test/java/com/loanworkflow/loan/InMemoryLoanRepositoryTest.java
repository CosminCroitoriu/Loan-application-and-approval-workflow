package com.loanworkflow.loan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Clock;

import org.junit.jupiter.api.Test;

import com.loanworkflow.approval.Role;
import com.loanworkflow.approval.User;
import com.loanworkflow.scoring.RuleBasedScoringStrategy;

class InMemoryLoanRepositoryTest {

    private final InMemoryLoanRepository repository = new InMemoryLoanRepository();

    private static LoanService newLoan() {
        return new LoanService(new User("carol", Role.APPLICANT), new BigDecimal("5000"),
                new RuleBasedScoringStrategy(), Clock.systemUTC());
    }

    @Test
    void savedLoanCanBeFoundById() {
        LoanService loan = newLoan();
        String id = repository.save(loan);

        assertSame(loan, repository.findById(id).orElseThrow());
    }

    @Test
    void unknownIdIsEmpty() {
        assertTrue(repository.findById("nope").isEmpty());
    }

    @Test
    void everySaveGetsANewId() {
        assertNotEquals(repository.save(newLoan()), repository.save(newLoan()));
    }

    @Test
    void changesToTheLoanAreSeenThroughTheRepository() {
        LoanService loan = newLoan();
        String id = repository.save(loan);

        loan.checkDocuments();

        assertEquals(LoanStatus.DOCUMENTS_CHECKED, repository.findById(id).orElseThrow().loan().status());
    }
}

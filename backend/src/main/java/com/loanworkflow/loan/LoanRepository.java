package com.loanworkflow.loan;

import java.util.Optional;

/** Where loans are kept. In memory for now; a JPA version can replace it later. */
public interface LoanRepository {

    String save(LoanService loan);

    Optional<LoanService> findById(String id);
}

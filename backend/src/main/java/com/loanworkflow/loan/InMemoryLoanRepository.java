package com.loanworkflow.loan;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

@Repository
public class InMemoryLoanRepository implements LoanRepository {

    // Thread-safe, because requests can arrive at the same time
    private final Map<String, LoanService> loans = new ConcurrentHashMap<>();

    @Override
    public String save(LoanService loan) {
        String id = UUID.randomUUID().toString();
        loans.put(id, loan);
        return id;
    }

    @Override
    public Optional<LoanService> findById(String id) {
        return Optional.ofNullable(loans.get(id));
    }
}

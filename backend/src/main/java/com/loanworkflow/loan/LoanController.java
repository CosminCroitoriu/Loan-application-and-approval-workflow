package com.loanworkflow.loan;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Clock;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.loanworkflow.approval.Role;
import com.loanworkflow.approval.User;
import com.loanworkflow.scoring.RuleBasedScoringStrategy;

@RestController
public class LoanController {

    public record CreateLoanRequest(String applicant, BigDecimal amount) {
    }

    private final LoanRepository repository;

    public LoanController(LoanRepository repository) {
        this.repository = repository;
    }

    // A new application is saved and starts as SUBMITTED
    @PostMapping("/loans")
    public ResponseEntity<?> create(@RequestBody CreateLoanRequest request) {
        if (request.applicant() == null || request.applicant().isBlank()
                || request.amount() == null || request.amount().signum() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "applicant and positive amount required"));
        }
        LoanService loan = new LoanService(
                new User(request.applicant(), Role.APPLICANT),
                request.amount(),
                new RuleBasedScoringStrategy(),
                Clock.systemUTC());
        String id = repository.save(loan);
        return ResponseEntity.created(URI.create("/loans/" + id)).body(toResponse(id, loan));
    }

    @GetMapping("/loans/{id}")
    public ResponseEntity<LoanResponse> get(@PathVariable String id) {
        return repository.findById(id)
                .map(loan -> ResponseEntity.ok(toResponse(id, loan)))
                .orElse(ResponseEntity.notFound().build());
    }

    private static LoanResponse toResponse(String id, LoanService loan) {
        return new LoanResponse(id, loan.loan().status(), loan.amount());
    }
}

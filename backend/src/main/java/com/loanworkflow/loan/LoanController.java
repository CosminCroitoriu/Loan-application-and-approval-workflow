package com.loanworkflow.loan;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Clock;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.loanworkflow.approval.Role;
import com.loanworkflow.approval.User;
import com.loanworkflow.scoring.CreditProfile;
import com.loanworkflow.scoring.RuleBasedScoringStrategy;
import com.loanworkflow.scoring.ScoreResult;

@RestController
public class LoanController {

    public record CreateLoanRequest(String applicant, BigDecimal amount) {
    }

    public record ApproveRequest(String approver) {
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

    // An officer confirms the documents are complete
    @PostMapping("/loans/{id}/documents")
    public LoanResponse checkDocuments(@PathVariable String id) {
        LoanService loan = find(id);
        loan.checkDocuments();
        return toResponse(id, loan);
    }

    // The applicant's finances are scored; a HIGH risk loan is rejected right away
    @PostMapping("/loans/{id}/score")
    public ScoreResponse score(@PathVariable String id, @RequestBody CreditProfile profile) {
        LoanService loan = find(id);
        loan.score(profile);
        ScoreResult result = loan.scoreResult();
        return new ScoreResponse(id, loan.loan().status(), result.score(), result.risk());
    }

    // An approver signs off. Large loans need two different approvers.
    @PostMapping("/loans/{id}/approve")
    public LoanResponse approve(@PathVariable String id, @RequestBody ApproveRequest request) {
        if (request.approver() == null || request.approver().isBlank()) {
            throw new IllegalArgumentException("approver is required");
        }
        LoanService loan = find(id);
        loan.approve(new User(request.approver(), Role.APPROVER));
        return toResponse(id, loan);
    }

    // The money is paid out and the repayment period starts
    @PostMapping("/loans/{id}/disburse")
    public LoanResponse disburse(@PathVariable String id) {
        LoanService loan = find(id);
        loan.disburse();
        return toResponse(id, loan);
    }

    private LoanService find(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "loan not found"));
    }

    private static LoanResponse toResponse(String id, LoanService loan) {
        return new LoanResponse(id, loan.loan().status(), loan.amount());
    }
}

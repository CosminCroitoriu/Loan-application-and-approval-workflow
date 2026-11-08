package com.loanworkflow.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "loan_approvals",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_loan_approver", columnNames = {"loan_id", "approver_name"})
        }
)
public class ApprovalRecordEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private LoanApplicationEntity loanApplication;

    @Column(name = "approver_name", length = 50, nullable = false)
    private String approverName;

    @Column(name = "sequence_number", nullable = false)
    private int sequenceNumber;

    @Column(name = "approved_at", nullable = false)
    private Instant approvedAt;

    public ApprovalRecordEntity() {
    }

    public ApprovalRecordEntity(String approverName, int sequenceNumber, Instant approvedAt) {
        this.approverName = approverName;
        this.sequenceNumber = sequenceNumber;
        this.approvedAt = approvedAt;
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (approvedAt == null) {
            approvedAt = Instant.now();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LoanApplicationEntity getLoanApplication() {
        return loanApplication;
    }

    public void setLoanApplication(LoanApplicationEntity loanApplication) {
        this.loanApplication = loanApplication;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }
}

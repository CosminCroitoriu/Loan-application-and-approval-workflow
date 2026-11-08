package com.loanworkflow.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.loanworkflow.loan.LoanStatus;
import com.loanworkflow.loan.LoanType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "loan_applications")
public class LoanApplicationEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "applicant_username", length = 50, nullable = false)
    private String applicantUsername;

    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "EUR";

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", length = 20, nullable = false)
    private LoanType loanType = LoanType.PERSONAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private LoanStatus status = LoanStatus.SUBMITTED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentEntity> documents = new ArrayList<>();

    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNumber ASC")
    private List<ApprovalRecordEntity> approvals = new ArrayList<>();

    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("changedAt ASC")
    private List<StatusChangeEntity> statusHistory = new ArrayList<>();

    public LoanApplicationEntity() {
    }

    public LoanApplicationEntity(String applicantUsername, BigDecimal amount, LoanType loanType) {
        this.applicantUsername = applicantUsername;
        this.amount = amount;
        this.loanType = loanType != null ? loanType : LoanType.PERSONAL;
        this.status = LoanStatus.SUBMITTED;
    }

    public LoanApplicationEntity(String applicantUsername, BigDecimal amount) {
        this(applicantUsername, amount, LoanType.PERSONAL);
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (currency == null) {
            currency = "EUR";
        }
        if (loanType == null) {
            loanType = LoanType.PERSONAL;
        }
        if (status == null) {
            status = LoanStatus.SUBMITTED;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public void addDocument(DocumentEntity document) {
        documents.add(document);
        document.setLoanApplication(this);
    }

    public void addApproval(ApprovalRecordEntity approval) {
        approvals.add(approval);
        approval.setLoanApplication(this);
    }

    public void addStatusChange(StatusChangeEntity change) {
        statusHistory.add(change);
        change.setLoanApplication(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicantUsername() {
        return applicantUsername;
    }

    public void setApplicantUsername(String applicantUsername) {
        this.applicantUsername = applicantUsername;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LoanType getLoanType() {
        return loanType;
    }

    public void setLoanType(LoanType loanType) {
        this.loanType = loanType;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<DocumentEntity> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentEntity> documents) {
        this.documents = documents;
    }

    public List<ApprovalRecordEntity> getApprovals() {
        return approvals;
    }

    public void setApprovals(List<ApprovalRecordEntity> approvals) {
        this.approvals = approvals;
    }

    public List<StatusChangeEntity> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<StatusChangeEntity> statusHistory) {
        this.statusHistory = statusHistory;
    }
}

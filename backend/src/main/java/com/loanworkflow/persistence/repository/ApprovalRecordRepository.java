package com.loanworkflow.persistence.repository;

import java.util.List;

import com.loanworkflow.persistence.entity.ApprovalRecordEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalRecordRepository extends JpaRepository<ApprovalRecordEntity, String> {

    List<ApprovalRecordEntity> findByLoanApplicationIdOrderBySequenceNumberAsc(String loanId);

    boolean existsByLoanApplicationIdAndApproverName(String loanId, String approverName);

    long countByLoanApplicationId(String loanId);
}

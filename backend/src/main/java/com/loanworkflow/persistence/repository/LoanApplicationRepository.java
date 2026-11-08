package com.loanworkflow.persistence.repository;

import java.util.List;

import com.loanworkflow.loan.LoanStatus;
import com.loanworkflow.loan.LoanType;
import com.loanworkflow.persistence.entity.LoanApplicationEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplicationEntity, String> {

    List<LoanApplicationEntity> findByStatus(LoanStatus status);

    List<LoanApplicationEntity> findByApplicantUsername(String applicantUsername);

    List<LoanApplicationEntity> findByLoanType(LoanType loanType);
}

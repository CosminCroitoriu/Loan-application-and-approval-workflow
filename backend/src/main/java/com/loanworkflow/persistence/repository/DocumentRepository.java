package com.loanworkflow.persistence.repository;

import java.util.List;

import com.loanworkflow.document.DocumentStatus;
import com.loanworkflow.persistence.entity.DocumentEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {

    List<DocumentEntity> findByLoanApplicationId(String loanId);

    List<DocumentEntity> findByLoanApplicationIdAndStatus(String loanId, DocumentStatus status);

    long countByLoanApplicationIdAndStatus(String loanId, DocumentStatus status);
}

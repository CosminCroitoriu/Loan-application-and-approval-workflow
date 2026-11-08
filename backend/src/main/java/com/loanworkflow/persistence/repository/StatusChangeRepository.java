package com.loanworkflow.persistence.repository;

import java.util.List;

import com.loanworkflow.persistence.entity.StatusChangeEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusChangeRepository extends JpaRepository<StatusChangeEntity, Long> {

    List<StatusChangeEntity> findByLoanApplicationIdOrderByChangedAtAsc(String loanId);
}

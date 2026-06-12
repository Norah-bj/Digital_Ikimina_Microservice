package com.ikimina.repository;

import com.ikimina.model.Loan;
import com.ikimina.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByMemberId(Long memberId);
    List<Loan> findByGuarantorId(Long guarantorId);
    List<Loan> findByStatus(LoanStatus status);
    List<Loan> findByMemberIdAndStatusIn(Long memberId, List<LoanStatus> statuses);
    List<Loan> findByStatusInAndDueDateBefore(List<LoanStatus> statuses, LocalDate date);

    @Query("select sum(l.outstandingBalance) from Loan l where l.status in :statuses")
    BigDecimal sumOutstandingByStatuses(@Param("statuses") List<LoanStatus> statuses);

    @Query("select sum(l.interestAmount) from Loan l")
    BigDecimal sumExpectedInterest();
}

package com.ikimina.repository;

import com.ikimina.model.Repayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface RepaymentRepository extends JpaRepository<Repayment, Long> {
    List<Repayment> findByLoanId(Long loanId);
    void deleteByLoanId(Long loanId);

    @Query("select sum(r.amount) from Repayment r where r.loan.id = :loanId")
    BigDecimal sumByLoanId(@Param("loanId") Long loanId);

    @Query("select sum(r.amount) from Repayment r")
    BigDecimal sumAllRepayments();
}

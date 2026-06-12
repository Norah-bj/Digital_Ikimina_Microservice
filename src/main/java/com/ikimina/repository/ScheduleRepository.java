package com.ikimina.repository;

import com.ikimina.model.InstallmentStatus;
import com.ikimina.model.RepaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<RepaymentSchedule, Long> {
    List<RepaymentSchedule> findByLoanIdOrderByInstallmentNumberAsc(Long loanId);
    
    List<RepaymentSchedule> findByLoanIdAndStatusIn(Long loanId, List<InstallmentStatus> statuses);
    
    List<RepaymentSchedule> findByStatusAndDueDateBefore(InstallmentStatus status, LocalDate date);
    
    void deleteByLoanId(Long loanId);
    
    long countByLoanId(Long loanId);
    
    @Query("SELECT COUNT(r) FROM RepaymentSchedule r WHERE r.loan.id = :loanId AND r.status = :status")
    long countByLoanIdAndStatus(@Param("loanId") Long loanId, @Param("status") InstallmentStatus status);
}

package com.ikimina.service;

import com.ikimina.model.InstallmentStatus;
import com.ikimina.model.Loan;
import com.ikimina.model.LoanStatus;
import com.ikimina.model.RepaymentSchedule;
import com.ikimina.repository.LoanRepository;
import com.ikimina.repository.ScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleService.class);

    private final ScheduleRepository scheduleRepository;
    private final LoanRepository loanRepository;

    public ScheduleService(ScheduleRepository scheduleRepository, LoanRepository loanRepository) {
        this.scheduleRepository = scheduleRepository;
        this.loanRepository = loanRepository;
    }

    /**
     * Generate complete repayment schedule when loan is approved.
     * Creates N installment records where N = loan.repaymentMonths.
     * Each installment has: dueDate = approvalDate + N months, amount = monthlyInstallment,
     * status = PENDING, and descending remainingBalance.
     * 
     * @param loan The loan for which to generate the schedule
     * @return List of created RepaymentSchedule entities
     */
    public List<RepaymentSchedule> generateSchedule(Loan loan) {
        try {
            // Validate loan has ACTIVE status and approval date
            if (loan.getStatus() != LoanStatus.ACTIVE) {
                log.warn("Cannot generate schedule for loan {} with status {}", loan.getId(), loan.getStatus());
                return new ArrayList<>();
            }

            if (loan.getApprovalDate() == null) {
                log.warn("Cannot generate schedule for loan {} without approval date", loan.getId());
                return new ArrayList<>();
            }

            List<RepaymentSchedule> schedules = new ArrayList<>();
            BigDecimal remainingBalance = loan.getTotalPayable();
            BigDecimal totalInterest = loan.getInterestAmount();
            int months = loan.getRepaymentMonths();
            BigDecimal monthlyInterest = totalInterest.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);

            // Create N installment records where N = loan.repaymentMonths
            for (int i = 1; i <= loan.getRepaymentMonths(); i++) {
                // Calculate due date as approvalDate + installmentNumber months
                LocalDate dueDate = loan.getApprovalDate().plusMonths(i);

                // Set installment amount to loan.monthlyInstallment
                BigDecimal installmentAmount = loan.getMonthlyInstallment();

                BigDecimal interestAmount = monthlyInterest;
                BigDecimal principalAmount = installmentAmount.subtract(interestAmount);

                // Calculate remaining balance descending from totalPayable
                remainingBalance = remainingBalance.subtract(installmentAmount).setScale(2, RoundingMode.HALF_UP);

                RepaymentSchedule schedule = new RepaymentSchedule(
                    loan,
                    i,
                    dueDate,
                    installmentAmount,
                    InstallmentStatus.PENDING,
                    remainingBalance,
                    principalAmount,
                    interestAmount
                );

                schedules.add(schedule);
            }

            // Persist all records
            return scheduleRepository.saveAll(schedules);

        } catch (Exception e) {
            // Log error but don't throw to avoid blocking loan approval
            log.error("Failed to generate schedule for loan {}", loan.getId(), e);
            return new ArrayList<>();
        }
    }

    public void processRepayment(Loan loan, BigDecimal paymentAmount) {
        List<RepaymentSchedule> schedules = scheduleRepository.findByLoanIdOrderByInstallmentNumberAsc(loan.getId());
        BigDecimal remainingPayment = paymentAmount;

        for (RepaymentSchedule schedule : schedules) {
            if (schedule.getStatus() == InstallmentStatus.PENDING || schedule.getStatus() == InstallmentStatus.OVERDUE) {
                if (remainingPayment.compareTo(schedule.getInstallmentAmount()) >= 0) {
                    schedule.setStatus(InstallmentStatus.PAID);
                    remainingPayment = remainingPayment.subtract(schedule.getInstallmentAmount());
                } else if (remainingPayment.compareTo(BigDecimal.ZERO) > 0) {
                    // Partial payment, status remains pending but logic could be more complex here
                    // For now, if they don't pay the full installment, it remains pending.
                    remainingPayment = BigDecimal.ZERO;
                }
                
                if (remainingPayment.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
            }
        }
        scheduleRepository.saveAll(schedules);
    }
}

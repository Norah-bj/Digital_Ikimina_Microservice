package com.ikimina.service;

import com.ikimina.dto.LoanRequest;
import com.ikimina.dto.RepaymentRequest;
import com.ikimina.model.GuaranteeStatus;
import com.ikimina.model.Loan;
import com.ikimina.model.LoanStatus;
import com.ikimina.model.Member;
import com.ikimina.model.Repayment;
import com.ikimina.repository.LoanRepository;
import com.ikimina.repository.MemberRepository;
import com.ikimina.repository.RepaymentRepository;
import com.ikimina.repository.SavingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);
    private static final BigDecimal INTEREST_RATE = BigDecimal.valueOf(6);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal MAX_INSTALLMENT_SALARY_RATIO = BigDecimal.valueOf(0.60);

    private final LoanRepository loanRepository;
    private final MemberRepository memberRepository;
    private final SavingRepository savingRepository;
    private final RepaymentRepository repaymentRepository;
    private final ScheduleService scheduleService;

    public LoanService(LoanRepository loanRepository, MemberRepository memberRepository,
                       SavingRepository savingRepository, RepaymentRepository repaymentRepository,
                       ScheduleService scheduleService) {
        this.loanRepository = loanRepository;
        this.memberRepository = memberRepository;
        this.savingRepository = savingRepository;
        this.repaymentRepository = repaymentRepository;
        this.scheduleService = scheduleService;
    }

    public Loan requestLoan(LoanRequest request) {
        Long memberId = requireId(request.getMemberId(), "Member ID is required");
        Member member = findMember(memberId);
        validateBorrower(member);

        Long borrowerId = requireId(member.getId(), "Member ID is missing");
        BigDecimal totalSavings = zeroIfNull(savingRepository.sumByMemberId(borrowerId));
        BigDecimal amount = money(request.getAmount());
        int repaymentMonths = request.getRepaymentMonths() == null ? 6 : request.getRepaymentMonths();

        Loan loan = new Loan();
        loan.setMember(member);
        loan.setAmount(amount);
        loan.setPurpose(request.getPurpose());
        loan.setRepaymentMonths(repaymentMonths);
        loan.setInterestRate(INTEREST_RATE);
        loan.setStatus(LoanStatus.PENDING);
        applyFinancials(loan);
        validateInstallmentAgainstSalary(member, loan.getMonthlyInstallment());
        applyGuaranteeRules(loan, request, amount, totalSavings, member);

        return loanRepository.save(loan);
    }

    @Transactional(readOnly = true)
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Loan getLoanById(Long id) {
        return findLoan(id);
    }

    @Transactional(readOnly = true)
    public List<Loan> getLoansByMemberId(Long memberId) {
        Long safeMemberId = requireId(memberId, "Member ID is required");
        if (!memberRepository.existsById(safeMemberId)) {
            throw new RuntimeException("Member not found with id " + safeMemberId);
        }
        return loanRepository.findByMemberId(safeMemberId);
    }

    @Transactional(readOnly = true)
    public List<Loan> getLoansByStatus(LoanStatus status) {
        return loanRepository.findByStatus(status);
    }

    public Loan approveLoan(Long id) {
        Loan loan = findLoan(id);
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalArgumentException("Only pending loans can be approved");
        }
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setApprovalDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusMonths(loan.getRepaymentMonths()));
        loan = loanRepository.save(loan);
        scheduleService.generateSchedule(loan);
        return loan;
    }

    public Loan rejectLoan(Long id, String reason) {
        Loan loan = findLoan(id);
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalArgumentException("Only pending loans can be rejected");
        }
        loan.setStatus(LoanStatus.REJECTED);
        loan.setRejectionReason(reason);
        return loanRepository.save(loan);
    }

    public Loan cancelLoan(Long id) {
        Loan loan = findLoan(id);
        if (loan.getStatus() == LoanStatus.ACTIVE || loan.getStatus() == LoanStatus.REPAID) {
            throw new IllegalArgumentException("Active or repaid loans cannot be cancelled");
        }
        loan.setStatus(LoanStatus.CANCELLED);
        return loanRepository.save(loan);
    }

    public Repayment recordRepayment(Long loanId, RepaymentRequest request) {
        Loan loan = findLoan(loanId);
        if (loan.getStatus() != LoanStatus.ACTIVE && loan.getStatus() != LoanStatus.DEFAULTED) {
            throw new IllegalArgumentException("Repayments can only be recorded for active or defaulted loans");
        }

        BigDecimal paymentAmount = money(request.getAmount());
        if (paymentAmount.compareTo(loan.getOutstandingBalance()) > 0) {
            throw new IllegalArgumentException("Repayment amount cannot exceed outstanding balance");
        }

        BigDecimal remainingBalance = loan.getOutstandingBalance().subtract(paymentAmount).setScale(2, RoundingMode.HALF_UP);
        loan.setOutstandingBalance(remainingBalance);
        if (remainingBalance.compareTo(BigDecimal.ZERO) == 0) {
            loan.setStatus(LoanStatus.REPAID);
            loan.setGuaranteeStatus(GuaranteeStatus.RELEASED);
        }

        Repayment repayment = new Repayment();
        repayment.setLoan(loan);
        repayment.setAmount(paymentAmount);
        repayment.setPaymentDate(request.getPaymentDate() == null ? LocalDate.now() : request.getPaymentDate());
        repayment.setRemainingBalance(remainingBalance);
        repayment.setNote(request.getNote());

        loanRepository.save(loan);

        // Update schedule installment statuses. Wrapped in try-catch so that a
        // schedule-table problem never rolls back the repayment record itself.
        try {
            scheduleService.processRepayment(loan, paymentAmount);
        } catch (Exception e) {
            log.warn("Could not update repayment schedule for loan {}: {}", loanId, e.getMessage());
        }

        return repaymentRepository.save(repayment);
    }

    @Transactional(readOnly = true)
    public List<Repayment> getRepaymentsByLoanId(Long loanId) {
        Long safeLoanId = requireId(loanId, "Loan ID is required");
        if (!loanRepository.existsById(safeLoanId)) {
            throw new RuntimeException("Loan not found with id " + safeLoanId);
        }
        return repaymentRepository.findByLoanId(safeLoanId);
    }

    public void markOverdueLoansAsDefaulted() {
        List<Loan> overdueLoans = loanRepository.findByStatusInAndDueDateBefore(
                List.of(LoanStatus.ACTIVE),
                LocalDate.now()
        );
        overdueLoans.forEach(loan -> loan.setStatus(LoanStatus.DEFAULTED));
        loanRepository.saveAll(overdueLoans);
    }

    public void deleteLoan(Long id) {
        Long safeLoanId = requireId(id, "Loan ID is required");
        if (!loanRepository.existsById(safeLoanId)) {
            throw new RuntimeException("Loan not found with id " + safeLoanId);
        }
        repaymentRepository.deleteByLoanId(safeLoanId);
        loanRepository.deleteById(safeLoanId);
    }

    private void validateBorrower(Member member) {
        if (!Boolean.TRUE.equals(member.getIsActive())) {
            throw new IllegalArgumentException("Member must be active to request a loan");
        }
        if (member.getJoinDate() == null || member.getJoinDate().plusMonths(3).isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Member can only borrow after 3 months of membership");
        }
        boolean hasActiveLoan = !loanRepository.findByMemberIdAndStatusIn(
                requireId(member.getId(), "Member ID is missing"),
                List.of(LoanStatus.ACTIVE, LoanStatus.DEFAULTED)
        ).isEmpty();
        if (hasActiveLoan) {
            throw new IllegalArgumentException("Member already has an active or defaulted loan");
        }
    }

    private void applyGuaranteeRules(Loan loan, LoanRequest request, BigDecimal amount,
                                     BigDecimal totalSavings, Member borrower) {
        if (amount.compareTo(totalSavings) <= 0) {
            loan.setGuaranteeStatus(GuaranteeStatus.NOT_REQUIRED);
            loan.setGuaranteeAmount(BigDecimal.ZERO);
            return;
        }

        BigDecimal requiredGuarantee = amount.subtract(totalSavings).setScale(2, RoundingMode.HALF_UP);
        if (request.getGuarantorId() == null) {
            throw new IllegalArgumentException("A guarantor is required when loan amount exceeds total savings");
        }
        Long borrowerId = requireId(borrower.getId(), "Borrower ID is missing");
        Long guarantorId = requireId(request.getGuarantorId(), "Guarantor ID is required");
        if (guarantorId.equals(borrowerId)) {
            throw new IllegalArgumentException("Borrower cannot guarantee their own loan");
        }

        Member guarantor = findMember(guarantorId);
        if (!Boolean.TRUE.equals(guarantor.getIsActive())) {
            throw new IllegalArgumentException("Guarantor must be active");
        }

        BigDecimal guaranteeAmount = request.getGuaranteeAmount() == null ? requiredGuarantee : request.getGuaranteeAmount();
        if (guaranteeAmount.compareTo(requiredGuarantee) < 0) {
            throw new IllegalArgumentException("Guarantee amount must be at least " + requiredGuarantee + " RWF");
        }

        BigDecimal guarantorSavings = zeroIfNull(savingRepository.sumByMemberId(requireId(guarantor.getId(), "Guarantor ID is missing")));
        if (guarantorSavings.compareTo(guaranteeAmount) < 0) {
            throw new IllegalArgumentException("Guarantor does not have enough savings to cover the guarantee");
        }

        loan.setGuarantor(guarantor);
        loan.setGuaranteeAmount(guaranteeAmount.setScale(2, RoundingMode.HALF_UP));
        loan.setGuaranteeStatus(GuaranteeStatus.ACCEPTED);
    }

    private void applyFinancials(Loan loan) {
        BigDecimal amount = money(loan.getAmount());
        BigDecimal interestAmount = amount.multiply(INTEREST_RATE).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal totalPayable = amount.add(interestAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthlyInstallment = totalPayable.divide(
                BigDecimal.valueOf(loan.getRepaymentMonths()),
                2,
                RoundingMode.HALF_UP
        );

        loan.setInterestAmount(interestAmount);
        loan.setTotalPayable(totalPayable);
        loan.setOutstandingBalance(totalPayable);
        loan.setMonthlyInstallment(monthlyInstallment);
    }

    private void validateInstallmentAgainstSalary(Member member, BigDecimal monthlyInstallment) {
        if (member.getMonthlySalary() == null || member.getMonthlySalary().compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        BigDecimal maxAllowed = member.getMonthlySalary().multiply(MAX_INSTALLMENT_SALARY_RATIO);
        if (monthlyInstallment.compareTo(maxAllowed) > 0) {
            throw new IllegalArgumentException("Monthly installment exceeds 60% of member salary");
        }
    }

    private Member findMember(Long id) {
        Long safeId = requireId(id, "Member ID is required");
        return memberRepository.findById(safeId)
                .orElseThrow(() -> new RuntimeException("Member not found with id " + safeId));
    }

    private Loan findLoan(Long id) {
        Long safeId = requireId(id, "Loan ID is required");
        return loanRepository.findById(safeId)
                .orElseThrow(() -> new RuntimeException("Loan not found with id " + safeId));
    }

    private BigDecimal money(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private @NonNull Long requireId(Long id, String message) {
        if (id == null) {
            throw new IllegalArgumentException(message);
        }
        return id;
    }
}

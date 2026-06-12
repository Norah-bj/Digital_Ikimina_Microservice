package com.ikimina.service;

import com.ikimina.dto.DashboardResponse;
import com.ikimina.model.LoanStatus;
import com.ikimina.repository.LoanRepository;
import com.ikimina.repository.MemberRepository;
import com.ikimina.repository.RepaymentRepository;
import com.ikimina.repository.SavingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final SavingRepository savingRepository;
    private final RepaymentRepository repaymentRepository;

    public DashboardService(MemberRepository memberRepository, LoanRepository loanRepository,
                            SavingRepository savingRepository, RepaymentRepository repaymentRepository) {
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
        this.savingRepository = savingRepository;
        this.repaymentRepository = repaymentRepository;
    }

    public DashboardResponse getDashboard() {
        long totalMembers = memberRepository.count();
        long activeMembers = memberRepository.findAll().stream()
                .filter(member -> Boolean.TRUE.equals(member.getIsActive()))
                .count();
        long totalLoans = loanRepository.count();
        long pendingLoans = loanRepository.findByStatus(LoanStatus.PENDING).size();

        return new DashboardResponse(
                totalMembers,
                activeMembers,
                totalLoans,
                pendingLoans,
                zeroIfNull(savingRepository.sumAllSavings()),
                zeroIfNull(loanRepository.sumOutstandingByStatuses(List.of(LoanStatus.ACTIVE, LoanStatus.DEFAULTED))),
                zeroIfNull(repaymentRepository.sumAllRepayments()),
                zeroIfNull(loanRepository.sumExpectedInterest())
        );
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}

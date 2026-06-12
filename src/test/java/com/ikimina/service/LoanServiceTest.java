package com.ikimina.service;

import com.ikimina.dto.LoanRequest;
import com.ikimina.model.Loan;
import com.ikimina.model.LoanStatus;
import com.ikimina.model.Member;
import com.ikimina.repository.LoanRepository;
import com.ikimina.repository.MemberRepository;
import com.ikimina.repository.RepaymentRepository;
import com.ikimina.repository.SavingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SavingRepository savingRepository;

    @Mock
    private RepaymentRepository repaymentRepository;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private LoanService loanService;

    private Member validMember;

    @BeforeEach
    void setUp() {
        validMember = new Member();
        validMember.setId(1L);
        validMember.setIsActive(true);
        validMember.setJoinDate(LocalDate.now().minusMonths(4)); // Eligible (> 3 months)
        validMember.setMonthlySalary(new BigDecimal("1000000"));
    }

    @Test
    void requestLoan_Success() {
        LoanRequest req = new LoanRequest();
        req.setMemberId(1L);
        req.setAmount(new BigDecimal("100000"));
        req.setRepaymentMonths(6);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(validMember));
        when(savingRepository.sumByMemberId(1L)).thenReturn(new BigDecimal("200000"));
        when(loanRepository.findByMemberIdAndStatusIn(anyLong(), anyList())).thenReturn(Collections.emptyList());
        
        Loan savedLoan = new Loan();
        savedLoan.setId(1L);
        savedLoan.setStatus(LoanStatus.PENDING);
        savedLoan.setAmount(new BigDecimal("100000"));
        when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);

        Loan result = loanService.requestLoan(req);

        assertNotNull(result);
        assertEquals(LoanStatus.PENDING, result.getStatus());
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    void approveLoan_Success() {
        Loan pendingLoan = new Loan();
        pendingLoan.setId(10L);
        pendingLoan.setStatus(LoanStatus.PENDING);
        pendingLoan.setRepaymentMonths(6);

        when(loanRepository.findById(10L)).thenReturn(Optional.of(pendingLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(pendingLoan);

        Loan approvedLoan = loanService.approveLoan(10L);

        assertEquals(LoanStatus.ACTIVE, approvedLoan.getStatus());
        assertNotNull(approvedLoan.getApprovalDate());
        verify(scheduleService, times(1)).generateSchedule(pendingLoan);
        verify(loanRepository, times(1)).save(pendingLoan);
    }

    @Test
    void rejectLoan_Success() {
        Loan pendingLoan = new Loan();
        pendingLoan.setId(10L);
        pendingLoan.setStatus(LoanStatus.PENDING);

        when(loanRepository.findById(10L)).thenReturn(Optional.of(pendingLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(pendingLoan);

        Loan rejectedLoan = loanService.rejectLoan(10L, "Not eligible");

        assertEquals(LoanStatus.REJECTED, rejectedLoan.getStatus());
        assertEquals("Not eligible", rejectedLoan.getRejectionReason());
        verify(loanRepository, times(1)).save(pendingLoan);
    }
}

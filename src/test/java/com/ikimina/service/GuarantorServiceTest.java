package com.ikimina.service;

import com.ikimina.dto.LoanRequest;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GuarantorServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SavingRepository savingRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private RepaymentRepository repaymentRepository;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private LoanService loanService;

    private Member borrower;
    private Member guarantor;

    @BeforeEach
    void setUp() {
        borrower = new Member();
        borrower.setId(1L);
        borrower.setIsActive(true);
        borrower.setJoinDate(LocalDate.now().minusMonths(6));

        guarantor = new Member();
        guarantor.setId(2L);
        guarantor.setIsActive(true);
        guarantor.setJoinDate(LocalDate.now().minusMonths(12));
    }

    @Test
    void requestLoan_WithoutGuarantor_SufficientSavings_Success() {
        LoanRequest req = new LoanRequest();
        req.setMemberId(1L);
        req.setAmount(new BigDecimal("100000"));

        borrower.setMonthlySalary(new BigDecimal("1000000")); // salary required for installment check
        when(memberRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(savingRepository.sumByMemberId(1L)).thenReturn(new BigDecimal("150000")); // > amount
        when(loanRepository.findByMemberIdAndStatusIn(anyLong(), anyList())).thenReturn(Collections.emptyList());
        when(loanRepository.save(any(com.ikimina.model.Loan.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> loanService.requestLoan(req));
    }

    @Test
    void requestLoan_GuarantorRequired_MissingGuarantor_ThrowsException() {
        LoanRequest req = new LoanRequest();
        req.setMemberId(1L);
        req.setAmount(new BigDecimal("500000"));

        when(memberRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(savingRepository.sumByMemberId(1L)).thenReturn(new BigDecimal("150000")); // < amount
        when(loanRepository.findByMemberIdAndStatusIn(anyLong(), anyList())).thenReturn(Collections.emptyList());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> loanService.requestLoan(req));
        assertEquals("A guarantor is required when loan amount exceeds total savings", ex.getMessage());
    }

    @Test
    void requestLoan_GuarantorInsufficientSavings_ThrowsException() {
        LoanRequest req = new LoanRequest();
        req.setMemberId(1L);
        req.setAmount(new BigDecimal("500000"));
        req.setGuarantorId(2L);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(savingRepository.sumByMemberId(1L)).thenReturn(new BigDecimal("150000")); // needs 350,000 guarantee
        when(loanRepository.findByMemberIdAndStatusIn(anyLong(), anyList())).thenReturn(Collections.emptyList());
        
        when(memberRepository.findById(2L)).thenReturn(Optional.of(guarantor));
        when(savingRepository.sumByMemberId(2L)).thenReturn(new BigDecimal("200000")); // 200k < 350k

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> loanService.requestLoan(req));
        assertEquals("Guarantor does not have enough savings to cover the guarantee", ex.getMessage());
    }
}

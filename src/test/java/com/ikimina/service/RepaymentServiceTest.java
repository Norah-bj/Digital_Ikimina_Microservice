package com.ikimina.service;

import com.ikimina.dto.RepaymentRequest;
import com.ikimina.model.GuaranteeStatus;
import com.ikimina.model.Loan;
import com.ikimina.model.LoanStatus;
import com.ikimina.model.Repayment;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RepaymentServiceTest {

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
    private LoanService loanService; // Tests repayment logic housed in LoanService

    private Loan activeLoan;

    @BeforeEach
    void setUp() {
        activeLoan = new Loan();
        activeLoan.setId(1L);
        activeLoan.setStatus(LoanStatus.ACTIVE);
        activeLoan.setOutstandingBalance(new BigDecimal("100000"));
        activeLoan.setGuaranteeStatus(GuaranteeStatus.ACCEPTED);
    }

    @Test
    void recordRepayment_PartialPayment_UpdatesBalance() {
        RepaymentRequest req = new RepaymentRequest();
        req.setAmount(new BigDecimal("40000"));
        req.setPaymentDate(LocalDate.now());

        when(loanRepository.findById(1L)).thenReturn(Optional.of(activeLoan));
        when(repaymentRepository.save(any(Repayment.class))).thenAnswer(i -> i.getArgument(0));

        Repayment repayment = loanService.recordRepayment(1L, req);

        assertNotNull(repayment);
        assertEquals(new BigDecimal("60000.00"), activeLoan.getOutstandingBalance());
        assertEquals(LoanStatus.ACTIVE, activeLoan.getStatus());
        verify(loanRepository, times(1)).save(activeLoan);
        verify(repaymentRepository, times(1)).save(any(Repayment.class));
    }

    @Test
    void recordRepayment_FullPayment_CompletesLoan() {
        RepaymentRequest req = new RepaymentRequest();
        req.setAmount(new BigDecimal("100000"));
        req.setPaymentDate(LocalDate.now());

        when(loanRepository.findById(1L)).thenReturn(Optional.of(activeLoan));
        when(repaymentRepository.save(any(Repayment.class))).thenAnswer(i -> i.getArgument(0));

        Repayment repayment = loanService.recordRepayment(1L, req);

        assertNotNull(repayment);
        assertEquals(new BigDecimal("0.00"), activeLoan.getOutstandingBalance());
        assertEquals(LoanStatus.REPAID, activeLoan.getStatus());
        assertEquals(GuaranteeStatus.RELEASED, activeLoan.getGuaranteeStatus());
        verify(loanRepository, times(1)).save(activeLoan);
        verify(repaymentRepository, times(1)).save(any(Repayment.class));
    }

    @Test
    void recordRepayment_ExceedsBalance_ThrowsException() {
        RepaymentRequest req = new RepaymentRequest();
        req.setAmount(new BigDecimal("150000"));

        when(loanRepository.findById(1L)).thenReturn(Optional.of(activeLoan));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> loanService.recordRepayment(1L, req));
        assertEquals("Repayment amount cannot exceed outstanding balance", ex.getMessage());
        verify(repaymentRepository, never()).save(any());
    }
}

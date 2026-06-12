package com.ikimina.service;

import com.ikimina.model.InstallmentStatus;
import com.ikimina.model.Loan;
import com.ikimina.model.LoanStatus;
import com.ikimina.model.Member;
import com.ikimina.model.RepaymentSchedule;
import com.ikimina.repository.LoanRepository;
import com.ikimina.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ScheduleService.generateSchedule method
 * Tests: Requirements 1.1, 1.2, 1.3, 1.4, 1.5
 */
@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    private Member member;
    private Loan loan3Months;
    private Loan loan12Months;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setId(1L);
        member.setFullName("Test Member");
    }

    /**
     * Test schedule generation for 3-month loan
     * Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5
     */
    @Test
    void testGenerateScheduleFor3MonthLoan() {
        // Arrange
        loan3Months = createLoan(new BigDecimal("100000.00"), 3);
        
        // Mock repository to return the schedules as-is
        when(scheduleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<RepaymentSchedule> schedules = scheduleService.generateSchedule(loan3Months);

        // Assert
        assertNotNull(schedules);
        assertEquals(3, schedules.size(), "Should create 3 installments for 3-month loan");

        // Verify first installment
        RepaymentSchedule firstInstallment = schedules.get(0);
        assertEquals(1, firstInstallment.getInstallmentNumber());
        assertEquals(loan3Months.getApprovalDate().plusMonths(1), firstInstallment.getDueDate());
        assertEquals(loan3Months.getMonthlyInstallment(), firstInstallment.getInstallmentAmount());
        assertEquals(InstallmentStatus.PENDING, firstInstallment.getStatus());

        // Verify last installment
        RepaymentSchedule lastInstallment = schedules.get(2);
        assertEquals(3, lastInstallment.getInstallmentNumber());
        assertEquals(loan3Months.getApprovalDate().plusMonths(3), lastInstallment.getDueDate());

        // Verify saveAll was called
        verify(scheduleRepository, times(1)).saveAll(anyList());
    }

    /**
     * Test schedule generation for 12-month loan
     * Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5
     */
    @Test
    void testGenerateScheduleFor12MonthLoan() {
        // Arrange
        loan12Months = createLoan(new BigDecimal("1000000.00"), 12);
        
        // Mock repository to return the schedules as-is
        when(scheduleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<RepaymentSchedule> schedules = scheduleService.generateSchedule(loan12Months);

        // Assert
        assertNotNull(schedules);
        assertEquals(12, schedules.size(), "Should create 12 installments for 12-month loan");

        // Verify installment numbers are sequential
        for (int i = 0; i < 12; i++) {
            assertEquals(i + 1, schedules.get(i).getInstallmentNumber());
        }

        // Verify last installment
        RepaymentSchedule lastInstallment = schedules.get(11);
        assertEquals(12, lastInstallment.getInstallmentNumber());
        assertEquals(loan12Months.getApprovalDate().plusMonths(12), lastInstallment.getDueDate());

        // Verify saveAll was called
        verify(scheduleRepository, times(1)).saveAll(anyList());
    }

    /**
     * Test installment amounts sum to totalPayable (within rounding tolerance)
     * Validates: Requirements 1.3
     */
    @Test
    void testInstallmentAmountsSumToTotalPayable() {
        // Arrange
        loan3Months = createLoan(new BigDecimal("100000.00"), 3);
        
        // Mock repository to return the schedules as-is
        when(scheduleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<RepaymentSchedule> schedules = scheduleService.generateSchedule(loan3Months);

        // Assert
        BigDecimal sumOfInstallments = schedules.stream()
            .map(RepaymentSchedule::getInstallmentAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Allow 1 RWF tolerance for rounding differences
        BigDecimal difference = loan3Months.getTotalPayable().subtract(sumOfInstallments).abs();
        assertTrue(difference.compareTo(new BigDecimal("1.00")) <= 0,
            "Sum of installment amounts should equal total payable within 1 RWF tolerance. " +
            "Expected: " + loan3Months.getTotalPayable() + ", Actual: " + sumOfInstallments);
    }

    /**
     * Test due dates are correctly calculated
     * Validates: Requirements 1.2
     */
    @Test
    void testDueDatesAreCorrectlyCalculated() {
        // Arrange
        loan3Months = createLoan(new BigDecimal("100000.00"), 3);
        LocalDate approvalDate = loan3Months.getApprovalDate();
        
        // Mock repository to return the schedules as-is
        when(scheduleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<RepaymentSchedule> schedules = scheduleService.generateSchedule(loan3Months);

        // Assert
        for (int i = 0; i < schedules.size(); i++) {
            RepaymentSchedule schedule = schedules.get(i);
            LocalDate expectedDueDate = approvalDate.plusMonths(i + 1);
            assertEquals(expectedDueDate, schedule.getDueDate(),
                "Installment " + (i + 1) + " should have due date " + expectedDueDate);
        }
    }

    /**
     * Test remaining balances decrease correctly
     * Validates: Requirements 1.4
     */
    @Test
    void testRemainingBalancesDecreaseCorrectly() {
        // Arrange
        loan3Months = createLoan(new BigDecimal("100000.00"), 3);
        
        // Mock repository to return the schedules as-is
        when(scheduleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<RepaymentSchedule> schedules = scheduleService.generateSchedule(loan3Months);

        // Assert
        BigDecimal previousBalance = loan3Months.getTotalPayable();
        
        for (RepaymentSchedule schedule : schedules) {
            BigDecimal expectedBalance = previousBalance.subtract(schedule.getInstallmentAmount())
                .setScale(2, RoundingMode.HALF_UP);
            assertEquals(expectedBalance, schedule.getRemainingBalance(),
                "Remaining balance should decrease by installment amount");
            previousBalance = expectedBalance;
        }

        // Last installment should have remaining balance of 0.00
        RepaymentSchedule lastSchedule = schedules.get(schedules.size() - 1);
        assertEquals(new BigDecimal("0.00"), lastSchedule.getRemainingBalance(),
            "Last installment should have remaining balance of 0.00");
    }

    /**
     * Test all installments start with PENDING status
     * Validates: Requirements 1.5
     */
    @Test
    void testAllInstallmentsStartWithPendingStatus() {
        // Arrange
        loan3Months = createLoan(new BigDecimal("100000.00"), 3);
        
        // Mock repository to return the schedules as-is
        when(scheduleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<RepaymentSchedule> schedules = scheduleService.generateSchedule(loan3Months);

        // Assert
        for (RepaymentSchedule schedule : schedules) {
            assertEquals(InstallmentStatus.PENDING, schedule.getStatus(),
                "All installments should start with PENDING status");
        }
    }

    /**
     * Test error handling for missing approval date
     * Validates: Requirements 1.7
     */
    @Test
    void testErrorHandlingForMissingApprovalDate() {
        // Arrange
        loan3Months = createLoan(new BigDecimal("100000.00"), 3);
        loan3Months.setApprovalDate(null); // Missing approval date

        // Act
        List<RepaymentSchedule> schedules = scheduleService.generateSchedule(loan3Months);

        // Assert
        assertNotNull(schedules);
        assertTrue(schedules.isEmpty(), "Should return empty list when approval date is missing");
        
        // Verify saveAll was NOT called
        verify(scheduleRepository, never()).saveAll(anyList());
    }

    /**
     * Test error handling for non-ACTIVE loan status
     * Validates: Requirements 1.7
     */
    @Test
    void testErrorHandlingForNonActiveLoan() {
        // Arrange
        loan3Months = createLoan(new BigDecimal("100000.00"), 3);
        loan3Months.setStatus(LoanStatus.PENDING); // Not ACTIVE

        // Act
        List<RepaymentSchedule> schedules = scheduleService.generateSchedule(loan3Months);

        // Assert
        assertNotNull(schedules);
        assertTrue(schedules.isEmpty(), "Should return empty list when loan is not ACTIVE");
        
        // Verify saveAll was NOT called
        verify(scheduleRepository, never()).saveAll(anyList());
    }

    /**
     * Helper method to create a loan with specified amount and repayment months
     */
    private Loan createLoan(BigDecimal amount, int repaymentMonths) {
        Loan loan = new Loan(amount, member);
        loan.setId(1L);
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setApprovalDate(LocalDate.of(2025, 1, 15));
        loan.setRepaymentMonths(repaymentMonths);
        
        // Calculate financial details (same logic as LoanService)
        BigDecimal interestRate = new BigDecimal("6");
        BigDecimal interestAmount = amount.multiply(interestRate)
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal totalPayable = amount.add(interestAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthlyInstallment = totalPayable.divide(
            BigDecimal.valueOf(repaymentMonths), 
            2, 
            RoundingMode.HALF_UP
        );
        
        loan.setInterestRate(interestRate);
        loan.setInterestAmount(interestAmount);
        loan.setTotalPayable(totalPayable);
        loan.setOutstandingBalance(totalPayable);
        loan.setMonthlyInstallment(monthlyInstallment);
        
        return loan;
    }
}

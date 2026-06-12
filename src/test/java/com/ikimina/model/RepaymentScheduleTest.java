package com.ikimina.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RepaymentScheduleTest {

    private Loan loan;
    private RepaymentSchedule schedule;

    @BeforeEach
    void setUp() {
        // Create a test member
        Member member = new Member();
        member.setId(1L);
        member.setFullName("John Doe");
        
        // Create a test loan
        loan = new Loan();
        loan.setId(1L);
        loan.setAmount(new BigDecimal("1000000.00"));
        loan.setInterestRate(new BigDecimal("6.00"));
        loan.setInterestAmount(new BigDecimal("60000.00"));
        loan.setTotalPayable(new BigDecimal("1060000.00"));
        loan.setRepaymentMonths(6);
        loan.setMonthlyInstallment(new BigDecimal("176666.67"));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setApprovalDate(LocalDate.now());
        loan.setMember(member);
    }

    @Test
    void testDefaultConstructor() {
        RepaymentSchedule schedule = new RepaymentSchedule();
        assertNotNull(schedule);
        assertNull(schedule.getId());
        assertNull(schedule.getLoan());
        assertNull(schedule.getInstallmentNumber());
        assertNull(schedule.getDueDate());
        assertNull(schedule.getInstallmentAmount());
        assertNull(schedule.getStatus());
        assertNull(schedule.getRemainingBalance());
    }

    @Test
    void testParameterizedConstructor() {
        LocalDate dueDate = LocalDate.now().plusMonths(1);
        BigDecimal installmentAmount = new BigDecimal("176666.67");
        BigDecimal remainingBalance = new BigDecimal("883333.33");
        
        RepaymentSchedule schedule = new RepaymentSchedule(
            loan,
            1,
            dueDate,
            installmentAmount,
            InstallmentStatus.PENDING,
            remainingBalance,
            new BigDecimal("100000"),
            new BigDecimal("1000")
        );
        
        assertNotNull(schedule);
        assertEquals(loan, schedule.getLoan());
        assertEquals(1, schedule.getInstallmentNumber());
        assertEquals(dueDate, schedule.getDueDate());
        assertEquals(installmentAmount, schedule.getInstallmentAmount());
        assertEquals(InstallmentStatus.PENDING, schedule.getStatus());
        assertEquals(remainingBalance, schedule.getRemainingBalance());
    }

    @Test
    void testSettersAndGetters() {
        schedule = new RepaymentSchedule();
        
        schedule.setId(1L);
        assertEquals(1L, schedule.getId());
        
        schedule.setLoan(loan);
        assertEquals(loan, schedule.getLoan());
        
        schedule.setInstallmentNumber(2);
        assertEquals(2, schedule.getInstallmentNumber());
        
        LocalDate dueDate = LocalDate.now().plusMonths(2);
        schedule.setDueDate(dueDate);
        assertEquals(dueDate, schedule.getDueDate());
        
        BigDecimal installmentAmount = new BigDecimal("176666.67");
        schedule.setInstallmentAmount(installmentAmount);
        assertEquals(installmentAmount, schedule.getInstallmentAmount());
        
        schedule.setStatus(InstallmentStatus.PAID);
        assertEquals(InstallmentStatus.PAID, schedule.getStatus());
        
        BigDecimal remainingBalance = new BigDecimal("706666.66");
        schedule.setRemainingBalance(remainingBalance);
        assertEquals(remainingBalance, schedule.getRemainingBalance());
    }

    @Test
    void testInstallmentStatusEnumValues() {
        schedule = new RepaymentSchedule();
        
        schedule.setStatus(InstallmentStatus.PENDING);
        assertEquals(InstallmentStatus.PENDING, schedule.getStatus());
        
        schedule.setStatus(InstallmentStatus.PAID);
        assertEquals(InstallmentStatus.PAID, schedule.getStatus());
        
        schedule.setStatus(InstallmentStatus.OVERDUE);
        assertEquals(InstallmentStatus.OVERDUE, schedule.getStatus());
        
        schedule.setStatus(InstallmentStatus.PARTIALLY_PAID);
        assertEquals(InstallmentStatus.PARTIALLY_PAID, schedule.getStatus());
    }

    @Test
    void testMultipleInstallmentsForSameLoan() {
        LocalDate approvalDate = LocalDate.now();
        
        // Create first installment
        RepaymentSchedule installment1 = new RepaymentSchedule(
            loan,
            1,
            approvalDate.plusMonths(1),
            new BigDecimal("176666.67"),
            InstallmentStatus.PENDING,
            new BigDecimal("883333.33"),
            new BigDecimal("100000"),
            new BigDecimal("1000")
        );
        
        // Create second installment
        RepaymentSchedule installment2 = new RepaymentSchedule(
            loan,
            2,
            approvalDate.plusMonths(2),
            new BigDecimal("176666.67"),
            InstallmentStatus.PENDING,
            new BigDecimal("706666.66"),
            new BigDecimal("100000"),
            new BigDecimal("1000")
        );
        
        // Verify they reference the same loan but have different installment numbers
        assertEquals(loan, installment1.getLoan());
        assertEquals(loan, installment2.getLoan());
        assertNotEquals(installment1.getInstallmentNumber(), installment2.getInstallmentNumber());
    }

    @Test
    void testRemainingBalanceDecreases() {
        // Create three consecutive installments to verify remaining balance decreases
        BigDecimal totalPayable = loan.getTotalPayable();
        BigDecimal monthlyInstallment = loan.getMonthlyInstallment();
        
        RepaymentSchedule installment1 = new RepaymentSchedule(
            loan,
            1,
            LocalDate.now().plusMonths(1),
            monthlyInstallment,
            InstallmentStatus.PENDING,
            totalPayable.subtract(monthlyInstallment),
            new BigDecimal("100000"),
            new BigDecimal("1000")
        );
        
        RepaymentSchedule installment2 = new RepaymentSchedule(
            loan,
            2,
            LocalDate.now().plusMonths(2),
            monthlyInstallment,
            InstallmentStatus.PENDING,
            totalPayable.subtract(monthlyInstallment.multiply(new BigDecimal("2"))),
            new BigDecimal("100000"),
            new BigDecimal("1000")
        );
        
        RepaymentSchedule installment3 = new RepaymentSchedule(
            loan,
            3,
            LocalDate.now().plusMonths(3),
            monthlyInstallment,
            InstallmentStatus.PENDING,
            totalPayable.subtract(monthlyInstallment.multiply(new BigDecimal("3"))),
            new BigDecimal("100000"),
            new BigDecimal("1000")
        );
        
        // Verify remaining balance decreases with each installment
        assertTrue(installment1.getRemainingBalance().compareTo(installment2.getRemainingBalance()) > 0);
        assertTrue(installment2.getRemainingBalance().compareTo(installment3.getRemainingBalance()) > 0);
    }

    @Test
    void testDueDateCalculation() {
        LocalDate approvalDate = LocalDate.of(2025, 1, 15);
        
        RepaymentSchedule installment1 = new RepaymentSchedule(
            loan,
            1,
            approvalDate.plusMonths(1),
            loan.getMonthlyInstallment(),
            InstallmentStatus.PENDING,
            loan.getTotalPayable().subtract(loan.getMonthlyInstallment()),
            new BigDecimal("100000"),
            new BigDecimal("1000")
        );
        
        RepaymentSchedule installment2 = new RepaymentSchedule(
            loan,
            2,
            approvalDate.plusMonths(2),
            loan.getMonthlyInstallment(),
            InstallmentStatus.PENDING,
            loan.getTotalPayable().subtract(loan.getMonthlyInstallment().multiply(new BigDecimal("2"))),
            new BigDecimal("100000"),
            new BigDecimal("1000")
        );
        
        assertEquals(LocalDate.of(2025, 2, 15), installment1.getDueDate());
        assertEquals(LocalDate.of(2025, 3, 15), installment2.getDueDate());
    }
}

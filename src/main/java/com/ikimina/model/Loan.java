package com.ikimina.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    @DecimalMin(value = "1.0", message = "Loan amount must be greater than zero")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    private LocalDate requestDate;

    private LocalDate approvalDate;

    private LocalDate dueDate;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal interestAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPayable;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal outstandingBalance;

    @Column(nullable = false)
    @Min(1)
    private Integer repaymentMonths;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyInstallment;

    private String purpose;

    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GuaranteeStatus guaranteeStatus;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("loan")
    private List<RepaymentSchedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("loan")
    private List<Repayment> repayments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guarantor_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "savings", "loans"})
    private Member guarantor;

    @Column(precision = 19, scale = 2)
    private BigDecimal guaranteeAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "savings", "loans"})
    private Member member;

    public Loan() {
        this.requestDate = LocalDate.now();
        this.interestRate = BigDecimal.valueOf(6);
        this.interestAmount = BigDecimal.ZERO;
        this.totalPayable = BigDecimal.ZERO;
        this.outstandingBalance = BigDecimal.ZERO;
        this.monthlyInstallment = BigDecimal.ZERO;
        this.repaymentMonths = 1;
        this.status = LoanStatus.PENDING;
        this.guaranteeStatus = GuaranteeStatus.NOT_REQUIRED;
    }

    public Loan(BigDecimal amount, Member member) {
        this();
        this.amount = amount;
        this.member = member;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDate getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDate approvalDate) {
        this.approvalDate = approvalDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public BigDecimal getInterestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(BigDecimal interestAmount) {
        this.interestAmount = interestAmount;
    }

    public BigDecimal getTotalPayable() {
        return totalPayable;
    }

    public void setTotalPayable(BigDecimal totalPayable) {
        this.totalPayable = totalPayable;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(BigDecimal outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public Integer getRepaymentMonths() {
        return repaymentMonths;
    }

    public void setRepaymentMonths(Integer repaymentMonths) {
        this.repaymentMonths = repaymentMonths;
    }

    public BigDecimal getMonthlyInstallment() {
        return monthlyInstallment;
    }

    public void setMonthlyInstallment(BigDecimal monthlyInstallment) {
        this.monthlyInstallment = monthlyInstallment;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public GuaranteeStatus getGuaranteeStatus() {
        return guaranteeStatus;
    }

    public void setGuaranteeStatus(GuaranteeStatus guaranteeStatus) {
        this.guaranteeStatus = guaranteeStatus;
    }

    public List<RepaymentSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<RepaymentSchedule> schedules) {
        this.schedules = schedules;
    }

    public List<Repayment> getRepayments() {
        return repayments;
    }

    public void setRepayments(List<Repayment> repayments) {
        this.repayments = repayments;
    }

    public Member getGuarantor() {
        return guarantor;
    }

    public void setGuarantor(Member guarantor) {
        this.guarantor = guarantor;
    }

    public BigDecimal getGuaranteeAmount() {
        return guaranteeAmount;
    }

    public void setGuaranteeAmount(BigDecimal guaranteeAmount) {
        this.guaranteeAmount = guaranteeAmount;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }
}

package com.ikimina.dto;

import java.math.BigDecimal;

public class ScheduleSummary {

    private Long loanId;
    private int totalInstallments;
    private int paidInstallments;
    private int overdueInstallments;
    private int pendingInstallments;
    private int partiallyPaidInstallments;
    private BigDecimal totalPaid;
    private BigDecimal totalPending;

    public ScheduleSummary() {
    }

    public ScheduleSummary(Long loanId, int totalInstallments, int paidInstallments, int overdueInstallments,
                           int pendingInstallments, int partiallyPaidInstallments,
                           BigDecimal totalPaid, BigDecimal totalPending) {
        this.loanId = loanId;
        this.totalInstallments = totalInstallments;
        this.paidInstallments = paidInstallments;
        this.overdueInstallments = overdueInstallments;
        this.pendingInstallments = pendingInstallments;
        this.partiallyPaidInstallments = partiallyPaidInstallments;
        this.totalPaid = totalPaid;
        this.totalPending = totalPending;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public int getTotalInstallments() {
        return totalInstallments;
    }

    public void setTotalInstallments(int totalInstallments) {
        this.totalInstallments = totalInstallments;
    }

    public int getPaidInstallments() {
        return paidInstallments;
    }

    public void setPaidInstallments(int paidInstallments) {
        this.paidInstallments = paidInstallments;
    }

    public int getOverdueInstallments() {
        return overdueInstallments;
    }

    public void setOverdueInstallments(int overdueInstallments) {
        this.overdueInstallments = overdueInstallments;
    }

    public int getPendingInstallments() {
        return pendingInstallments;
    }

    public void setPendingInstallments(int pendingInstallments) {
        this.pendingInstallments = pendingInstallments;
    }

    public int getPartiallyPaidInstallments() {
        return partiallyPaidInstallments;
    }

    public void setPartiallyPaidInstallments(int partiallyPaidInstallments) {
        this.partiallyPaidInstallments = partiallyPaidInstallments;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    public BigDecimal getTotalPending() {
        return totalPending;
    }

    public void setTotalPending(BigDecimal totalPending) {
        this.totalPending = totalPending;
    }
}

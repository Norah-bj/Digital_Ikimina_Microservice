package com.ikimina.dto;

import java.math.BigDecimal;

public class DashboardResponse {

    private long totalMembers;
    private long activeMembers;
    private long totalLoans;
    private long pendingLoans;
    private BigDecimal totalSavings;
    private BigDecimal outstandingLoans;
    private BigDecimal totalRepayments;
    private BigDecimal expectedInterest;

    public DashboardResponse(long totalMembers, long activeMembers, long totalLoans, long pendingLoans,
                             BigDecimal totalSavings, BigDecimal outstandingLoans,
                             BigDecimal totalRepayments, BigDecimal expectedInterest) {
        this.totalMembers = totalMembers;
        this.activeMembers = activeMembers;
        this.totalLoans = totalLoans;
        this.pendingLoans = pendingLoans;
        this.totalSavings = totalSavings;
        this.outstandingLoans = outstandingLoans;
        this.totalRepayments = totalRepayments;
        this.expectedInterest = expectedInterest;
    }

    public long getTotalMembers() {
        return totalMembers;
    }

    public long getActiveMembers() {
        return activeMembers;
    }

    public long getTotalLoans() {
        return totalLoans;
    }

    public long getPendingLoans() {
        return pendingLoans;
    }

    public BigDecimal getTotalSavings() {
        return totalSavings;
    }

    public BigDecimal getOutstandingLoans() {
        return outstandingLoans;
    }

    public BigDecimal getTotalRepayments() {
        return totalRepayments;
    }

    public BigDecimal getExpectedInterest() {
        return expectedInterest;
    }
}

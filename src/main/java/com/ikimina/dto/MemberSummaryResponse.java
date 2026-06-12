package com.ikimina.dto;

import java.math.BigDecimal;

public class MemberSummaryResponse {

    private Long memberId;
    private String fullName;
    private BigDecimal totalSavings;
    private BigDecimal shareValue;
    private BigDecimal activeLoanBalance;
    private BigDecimal monthlyContributionEstimate;
    private long savingsCount;
    private long loansCount;

    public MemberSummaryResponse(Long memberId, String fullName, BigDecimal totalSavings, BigDecimal shareValue,
                                 BigDecimal activeLoanBalance, BigDecimal monthlyContributionEstimate,
                                 long savingsCount, long loansCount) {
        this.memberId = memberId;
        this.fullName = fullName;
        this.totalSavings = totalSavings;
        this.shareValue = shareValue;
        this.activeLoanBalance = activeLoanBalance;
        this.monthlyContributionEstimate = monthlyContributionEstimate;
        this.savingsCount = savingsCount;
        this.loansCount = loansCount;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getFullName() {
        return fullName;
    }

    public BigDecimal getTotalSavings() {
        return totalSavings;
    }

    public BigDecimal getShareValue() {
        return shareValue;
    }

    public BigDecimal getActiveLoanBalance() {
        return activeLoanBalance;
    }

    public BigDecimal getMonthlyContributionEstimate() {
        return monthlyContributionEstimate;
    }

    public long getSavingsCount() {
        return savingsCount;
    }

    public long getLoansCount() {
        return loansCount;
    }
}

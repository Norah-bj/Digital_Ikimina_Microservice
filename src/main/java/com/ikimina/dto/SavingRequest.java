package com.ikimina.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SavingRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "5000.0", message = "Saving amount must be at least 5000 RWF")
    private BigDecimal amount;

    @NotBlank(message = "Saving month is required, example: 2026-05")
    private String savingMonth;

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getSavingMonth() {
        return savingMonth;
    }

    public void setSavingMonth(String savingMonth) {
        this.savingMonth = savingMonth;
    }
}

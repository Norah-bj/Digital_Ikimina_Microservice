package com.ikimina.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "savings")
public class Saving {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    @DecimalMin(value = "5000.0", message = "Saving amount must be at least 5000 RWF")
    private BigDecimal amount;

    @Column(nullable = false)
    @NotBlank(message = "Saving month is required")
    private String savingMonth;

    private LocalDate createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Member member;

    public Saving() {
        this.createdAt = LocalDate.now();
    }

    public Saving(BigDecimal amount, String savingMonth, Member member) {
        this();
        this.amount = amount;
        this.savingMonth = savingMonth;
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

    public String getSavingMonth() {
        return savingMonth;
    }

    public void setSavingMonth(String savingMonth) {
        this.savingMonth = savingMonth;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }
}

package com.finance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

/** One budget per month, stored as "yyyy-MM". */
@Entity
@Table(name = "budgets", uniqueConstraints = @UniqueConstraint(columnNames = {"month", "user_id"}))
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 7)
    private String month;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    public Budget() {}

    public Budget(Long userId, String month, BigDecimal amount) {
        this.userId = userId;
        this.month = month;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}

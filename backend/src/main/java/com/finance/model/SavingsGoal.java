
package com.finance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "savings_goals")
public class SavingsGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal targetAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal savedAmount = BigDecimal.ZERO;

    private LocalDate targetDate;

    public SavingsGoal() {}

    public SavingsGoal(Long userId, String name,
                       BigDecimal targetAmount, LocalDate targetDate) {
        this.userId = userId;
        this.name = name;
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public BigDecimal getSavedAmount() { return savedAmount; }
    public void setSavedAmount(BigDecimal savedAmount) {
        this.savedAmount = savedAmount;
    }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }
}

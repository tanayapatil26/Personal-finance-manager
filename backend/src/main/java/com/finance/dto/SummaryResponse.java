package com.finance.dto;

import java.math.BigDecimal;

public record SummaryResponse(
        String month,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal balance,
        BigDecimal budget,
        BigDecimal remainingBudget,
        boolean budgetExceeded) {
}

package com.finance.dto;

import java.math.BigDecimal;
import java.util.List;

public record ForecastResponse(
        List<MonthlyPoint> history,
        String nextMonth,
        BigDecimal predictedExpense,
        String trend,
        String method) {
}

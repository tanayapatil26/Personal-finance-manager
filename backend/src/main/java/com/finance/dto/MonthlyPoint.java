package com.finance.dto;

import java.math.BigDecimal;

public record MonthlyPoint(String month, BigDecimal income, BigDecimal expense) {
}

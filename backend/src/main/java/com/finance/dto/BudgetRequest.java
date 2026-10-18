package com.finance.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BudgetRequest(
        @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}", message = "Month must look like 2026-10")
        String month,

        @NotNull @DecimalMin(value = "0.01", message = "Budget must be greater than 0")
        BigDecimal amount) {
}

package com.finance.dto;

import com.finance.model.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotBlank(message = "Category is required")
        @Size(max = 60, message = "Category must be 60 characters or fewer")
        String category,

        @NotNull(message = "Type must be INCOME or EXPENSE")
        TransactionType type,

        LocalDate date,

        @Size(max = 255, message = "Description must be 255 characters or fewer")
        String description) {
}

package com.finance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 30, message = "Username must be 3 to 30 characters")
        @Pattern(
                regexp = "[A-Za-z0-9_.-]+",
                message = "Username can use letters, numbers, dot, dash and underscore"
        )
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 72, message = "Password must be 6 to 72 characters")
        String password
) {
}

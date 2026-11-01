package com.finance.controller;

import com.finance.service.PasswordResetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    public record ForgotPasswordRequest(
            @NotBlank(message = "Email is required") @Email(message = "Enter a valid email address") @Size(max = 254, message = "Email must not exceed 254 characters") String email) {
    }

    public record ResetPasswordRequest(
            @NotBlank(message = "Reset token is required") String token,

            @NotBlank(message = "New password is required") @Size(min = 6, max = 72, message = "Password must be 6 to 72 characters") String newPassword) {
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public java.util.Map<String, String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        passwordResetService.requestReset(request.email());

        // Generic response avoids revealing whether an account exists.
        return java.util.Map.of(
                "message",
                "If an account exists for that email, a password-reset link will be sent.");
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.OK)
    public java.util.Map<String, String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordResetService.resetPassword(
                request.token(),
                request.newPassword());

        return java.util.Map.of("message", "Password reset successfully.");
    }
}
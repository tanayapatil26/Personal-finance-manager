
package com.finance.controller;

import com.finance.model.SavingsGoal;
import com.finance.service.SavingsGoalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/savings-goals")
public class SavingsGoalController {

    private final SavingsGoalService service;

    public SavingsGoalController(SavingsGoalService service) {
        this.service = service;
    }

    @GetMapping
    public List<SavingsGoal> getAll(@AuthenticationPrincipal Long userId) {
        return service.getAll(userId);
    }

    @PostMapping
    public SavingsGoal create(
            @AuthenticationPrincipal Long userId,
            @RequestBody GoalRequest request) {
        return service.create(
            userId, request.name(), request.targetAmount(), request.targetDate()
        );
    }

    @PutMapping("/{id}")
    public SavingsGoal update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody GoalRequest request) {
        return service.update(
            userId, id, request.name(), request.targetAmount(),
            request.savedAmount(), request.targetDate()
        );
    }

    @DeleteMapping("/{id}")
    public void delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        service.delete(userId, id);
    }

    public record GoalRequest(
        String name,
        BigDecimal targetAmount,
        BigDecimal savedAmount,
        LocalDate targetDate
    ) {}
}

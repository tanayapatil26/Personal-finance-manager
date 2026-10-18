package com.finance.controller;

import com.finance.dto.BudgetRequest;
import com.finance.model.Budget;
import com.finance.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {

    private final BudgetService service;

    public BudgetController(BudgetService service) {
        this.service = service;
    }

    @PutMapping
    public Budget set(@AuthenticationPrincipal Long userId, @Valid @RequestBody BudgetRequest request) {
        return service.set(userId, request);
    }
}

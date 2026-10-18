package com.finance.controller;

import com.finance.dto.*;
import com.finance.model.TransactionType;
import com.finance.service.AnalyticsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AnalyticsController {

    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public SummaryResponse summary(@AuthenticationPrincipal Long userId,
                                   @RequestParam(required = false) String month) {
        return service.summary(userId, parse(month));
    }

    @GetMapping("/analytics/categories")
    public List<CategoryTotal> categories(@AuthenticationPrincipal Long userId,
                                          @RequestParam(required = false) String month,
                                          @RequestParam(defaultValue = "EXPENSE") TransactionType type) {
        return service.categoryBreakdown(userId, parse(month), type);
    }

    @GetMapping("/analytics/monthly")
    public List<MonthlyPoint> monthly(@AuthenticationPrincipal Long userId,
                                      @RequestParam(defaultValue = "6") int months) {
        return service.monthlyTrend(userId, Math.min(Math.max(months, 1), 24));
    }

    @GetMapping("/analytics/forecast")
    public ForecastResponse forecast(@AuthenticationPrincipal Long userId) {
        return service.forecastNextMonth(userId);
    }

    private YearMonth parse(String month) {
        return (month == null || month.isBlank()) ? YearMonth.now() : YearMonth.parse(month);
    }
}

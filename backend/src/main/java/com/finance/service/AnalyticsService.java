package com.finance.service;

import com.finance.dto.*;
import com.finance.model.Transaction;
import com.finance.model.TransactionType;
import com.finance.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final TransactionRepository transactions;
    private final BudgetService budgets;

    public AnalyticsService(TransactionRepository transactions, BudgetService budgets) {
        this.transactions = transactions;
        this.budgets = budgets;
    }

    public SummaryResponse summary(Long userId, YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        BigDecimal income = transactions.sumByTypeBetween(userId, TransactionType.INCOME, from, to);
        BigDecimal expenses = transactions.sumByTypeBetween(userId, TransactionType.EXPENSE, from, to);
        BigDecimal budget = budgets.getAmount(userId, month.toString());

        BigDecimal remaining = budget == null ? null : budget.subtract(expenses);
        boolean exceeded = budget != null && expenses.compareTo(budget) > 0;

        return new SummaryResponse(month.toString(), income, expenses, income.subtract(expenses),
                budget, remaining, exceeded);
    }

    public List<CategoryTotal> categoryBreakdown(Long userId, YearMonth month, TransactionType type) {
        List<CategoryTotal> result = new ArrayList<>();
        for (Object[] row : transactions.sumByCategory(userId, type, month.atDay(1), month.atEndOfMonth())) {
            result.add(new CategoryTotal((String) row[0], (BigDecimal) row[1]));
        }
        return result;
    }

    /** Income and expense totals for each of the last {@code months} months, oldest first. */
    public List<MonthlyPoint> monthlyTrend(Long userId, int months) {
        YearMonth end = YearMonth.now();
        YearMonth start = end.minusMonths(months - 1L);
        List<Transaction> all = transactions.findByUserIdAndDateBetween(userId, start.atDay(1), end.atEndOfMonth());

        List<MonthlyPoint> points = new ArrayList<>();
        for (YearMonth m = start; !m.isAfter(end); m = m.plusMonths(1)) {
            BigDecimal inc = BigDecimal.ZERO;
            BigDecimal exp = BigDecimal.ZERO;
            for (Transaction t : all) {
                if (YearMonth.from(t.getDate()).equals(m)) {
                    if (t.getType() == TransactionType.INCOME) inc = inc.add(t.getAmount());
                    else exp = exp.add(t.getAmount());
                }
            }
            points.add(new MonthlyPoint(m.toString(), inc, exp));
        }
        return points;
    }

    /**
     * Predicts next month's spending with ordinary least squares linear regression
     * over the last six months of expense totals (y = a + b*x).
     */
    public ForecastResponse forecastNextMonth(Long userId) {
        List<MonthlyPoint> history = monthlyTrend(userId, 6);
        int n = history.size();

        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        for (int i = 0; i < n; i++) {
            double y = history.get(i).expense().doubleValue();
            sumX += i;
            sumY += y;
            sumXY += i * y;
            sumXX += (double) i * i;
        }

        double denom = n * sumXX - sumX * sumX;
        double slope = denom == 0 ? 0 : (n * sumXY - sumX * sumY) / denom;
        double intercept = (sumY - slope * sumX) / n;
        double predicted = Math.max(0, intercept + slope * n);

        String trend = Math.abs(slope) < 1 ? "stable" : slope > 0 ? "rising" : "falling";

        return new ForecastResponse(
                history,
                YearMonth.now().plusMonths(1).toString(),
                BigDecimal.valueOf(predicted).setScale(2, RoundingMode.HALF_UP),
                trend,
                "Linear regression (least squares) on last 6 months of expenses");
    }
}

package com.finance.service;

import com.finance.dto.BudgetRequest;
import com.finance.model.Budget;
import com.finance.repository.BudgetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class BudgetService {

    private final BudgetRepository repository;

    public BudgetService(BudgetRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public BigDecimal getAmount(Long userId, String month) {
        return repository.findByUserIdAndMonth(userId, month).map(Budget::getAmount).orElse(null);
    }

    @Transactional
    public Budget set(Long userId, BudgetRequest req) {
        Budget b = repository.findByUserIdAndMonth(userId, req.month())
                .orElseGet(() -> new Budget(userId, req.month(), req.amount()));
        b.setAmount(req.amount());
        return repository.save(b);
    }
}

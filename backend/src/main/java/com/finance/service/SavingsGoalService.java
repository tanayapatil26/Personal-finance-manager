package com.finance.service;

import com.finance.exception.ResourceNotFoundException;

import com.finance.model.SavingsGoal;
import com.finance.repository.SavingsGoalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SavingsGoalService {

    private final SavingsGoalRepository repository;

    public SavingsGoalService(SavingsGoalRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<SavingsGoal> getAll(Long userId) {
        return repository.findAllByUserId(userId);
    }

    @Transactional
    public SavingsGoal create(Long userId, String name,
                              BigDecimal targetAmount, java.time.LocalDate targetDate) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Goal name is required.");
        }
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Target amount must be greater than zero.");
        }

        return repository.save(
            new SavingsGoal(userId, name.trim(), targetAmount, targetDate)
        );
    }

    @Transactional
    public SavingsGoal update(Long userId, Long id, String name,
                              BigDecimal targetAmount, BigDecimal savedAmount,
                              java.time.LocalDate targetDate) {
        SavingsGoal goal = repository.findByIdAndUserId(id, userId)
          .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found."));

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Goal name is required.");
        }
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Target amount must be greater than zero.");
        }
        if (savedAmount == null || savedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saved amount cannot be negative.");
        }

        goal.setName(name.trim());
        goal.setTargetAmount(targetAmount);
        goal.setSavedAmount(savedAmount);
        goal.setTargetDate(targetDate);

        return repository.save(goal);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        SavingsGoal goal = repository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found."));

        repository.delete(goal);
    }
}

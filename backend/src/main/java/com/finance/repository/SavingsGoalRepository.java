
package com.finance.repository;

import com.finance.model.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    List<SavingsGoal> findAllByUserId(Long userId);

    Optional<SavingsGoal> findByIdAndUserId(Long id, Long userId);
}

package com.finance.repository;

import com.finance.model.Transaction;
import com.finance.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
            select t from Transaction t
            where t.userId = :userId
              and (:category is null or lower(t.category) = lower(:category))
              and (:type is null or t.type = :type)
              and (:from is null or t.date >= :from)
              and (:to is null or t.date <= :to)
            order by t.date desc, t.id desc
            """)
    List<Transaction> search(@Param("userId") Long userId,
                             @Param("category") String category,
                             @Param("type") TransactionType type,
                             @Param("from") LocalDate from,
                             @Param("to") LocalDate to);

    @Query("""
            select coalesce(sum(t.amount), 0) from Transaction t
            where t.userId = :userId and t.type = :type and t.date between :from and :to
            """)
    BigDecimal sumByTypeBetween(@Param("userId") Long userId,
                                @Param("type") TransactionType type,
                                @Param("from") LocalDate from,
                                @Param("to") LocalDate to);

    @Query("""
            select t.category, sum(t.amount) from Transaction t
            where t.userId = :userId and t.type = :type and t.date between :from and :to
            group by t.category
            order by sum(t.amount) desc
            """)
    List<Object[]> sumByCategory(@Param("userId") Long userId,
                                 @Param("type") TransactionType type,
                                 @Param("from") LocalDate from,
                                 @Param("to") LocalDate to);

    List<Transaction> findByUserIdAndDateBetween(Long userId, LocalDate from, LocalDate to);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);
}

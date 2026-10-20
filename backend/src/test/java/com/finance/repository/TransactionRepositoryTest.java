
package com.finance.repository;

import com.finance.model.Transaction;
import com.finance.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:repositorytest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository repository;

    @Test
    void searchReturnsOnlyTransactionsForRequestedUser() {
        LocalDate today = LocalDate.now();

        repository.save(new Transaction(
            101L, new BigDecimal("100.00"), "Food",
            TransactionType.EXPENSE, today, "User A transaction"
        ));

        repository.save(new Transaction(
            202L, new BigDecimal("200.00"), "Travel",
            TransactionType.EXPENSE, today, "User B transaction"
        ));

        List<Transaction> results = repository.search(
            101L, null, null, null, null
        );

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUserId()).isEqualTo(101L);
        assertThat(results.get(0).getDescription())
            .isEqualTo("User A transaction");
    }
}

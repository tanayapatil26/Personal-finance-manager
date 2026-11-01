package com.finance;

import com.finance.model.Transaction;
import com.finance.model.TransactionType;
import com.finance.repository.TransactionRepository;
import com.finance.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import com.finance.dto.TransactionRequest;

import static org.mockito.Mockito.verify;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

        @Mock
        private TransactionRepository repository;

        @InjectMocks
        private TransactionService service;

        @Test
        void getReturnsTransactionBelongingToUser() {

                Transaction transaction = new Transaction(
                                101L,
                                new BigDecimal("200.00"),
                                "Food",
                                TransactionType.EXPENSE,
                                LocalDate.now(),
                                "Lunch");

                when(repository.findByIdAndUserId(1L, 101L))
                                .thenReturn(Optional.of(transaction));

                Transaction result = service.get(101L, 1L);

                assertThat(result.getAmount()).isEqualByComparingTo("200.00");
                assertThat(result.getCategory()).isEqualTo("Food");
        }

        @Test
        void getDoesNotReturnTransactionForAnotherUser() {

                when(repository.findByIdAndUserId(1L, 202L))
                                .thenReturn(Optional.empty());

                assertThrows(RuntimeException.class, () -> {
                        service.get(202L, 1L);
                });
        }

        @Test
        void createSavesTransactionForCorrectUser() {

                TransactionRequest request = new TransactionRequest(
                                new BigDecimal("300.00"),
                                "Food",
                                TransactionType.EXPENSE,
                                LocalDate.now(),
                                "Dinner");

                service.create(101L, request);

                ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

                verify(repository).save(captor.capture());

                Transaction saved = captor.getValue();

                assertThat(saved.getUserId()).isEqualTo(101L);
                assertThat(saved.getAmount())
                                .isEqualByComparingTo("300.00");
                assertThat(saved.getCategory()).isEqualTo("Food");
        }

        @Test
        void updateDoesNotModifyAnotherUsersTransaction() {

                when(repository.findByIdAndUserId(1L, 202L))
                                .thenReturn(Optional.empty());

                TransactionRequest request = new TransactionRequest(
                                new BigDecimal("400.00"),
                                "Travel",
                                TransactionType.EXPENSE,
                                LocalDate.now(),
                                "Trip");

                assertThrows(RuntimeException.class, () -> {
                        service.update(202L, 1L, request);
                });

                verify(repository, org.mockito.Mockito.never())
                                .save(org.mockito.ArgumentMatchers.any(Transaction.class));
        }

        @Test
        void deleteDoesNotDeleteAnotherUsersTransaction() {

                when(repository.findByIdAndUserId(1L, 202L))
                                .thenReturn(Optional.empty());

                assertThrows(RuntimeException.class, () -> {
                        service.delete(202L, 1L);
                });

                verify(repository, org.mockito.Mockito.never())
                                .delete(org.mockito.ArgumentMatchers.any(Transaction.class));
        }
}

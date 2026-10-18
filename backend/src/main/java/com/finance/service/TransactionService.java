package com.finance.service;

import com.finance.dto.TransactionRequest;
import com.finance.exception.ResourceNotFoundException;
import com.finance.model.Transaction;
import com.finance.model.TransactionType;
import com.finance.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Transaction> search(Long userId, String category, TransactionType type, LocalDate from, LocalDate to) {
        String cat = (category == null || category.isBlank()) ? null : category.trim();
        return repository.search(userId, cat, type, from, to);
    }

    @Transactional(readOnly = true)
    public Transaction get(Long userId, Long id) {
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction " + id + " not found"));
    }

    @Transactional
    public Transaction create(Long userId, TransactionRequest req) {
        Transaction t = new Transaction();
        t.setUserId(userId);
        apply(t, req);
        return repository.save(t);
    }

    @Transactional
    public Transaction update(Long userId, Long id, TransactionRequest req) {
        Transaction t = get(userId, id);
        apply(t, req);
        return repository.save(t);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        repository.delete(get(userId, id));
    }

    /**
     * Imports the transactions.txt file written by the original console app
     * (format per line: amount,category,type).
     */
    @Transactional
    public int importLegacy(Long userId, java.io.InputStream in) throws IOException {
        List<Transaction> batch = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3) continue;
                try {
                    BigDecimal amount = new BigDecimal(parts[0].trim());
                    TransactionType type = TransactionType.valueOf(parts[2].trim().toUpperCase());
                    batch.add(new Transaction(userId, amount, parts[1].trim(), type, LocalDate.now(), "Imported from console app"));
                } catch (IllegalArgumentException ignored) {
                    // skip malformed lines
                }
            }
        }
        repository.saveAll(batch);
        return batch.size();
    }

    private void apply(Transaction t, TransactionRequest req) {
        t.setAmount(req.amount());
        t.setCategory(req.category().trim());
        t.setType(req.type());
        t.setDate(req.date() != null ? req.date() : LocalDate.now());
        t.setDescription(req.description());
    }
}

package com.finance.controller;

import com.finance.dto.TransactionRequest;
import com.finance.model.Transaction;
import com.finance.model.TransactionType;
import com.finance.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @GetMapping
    public List<Transaction> list(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.search(userId, category, type, from, to);
    }

    @GetMapping("/{id}")
    public Transaction get(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return service.get(userId, id);
    }

    @PostMapping
    public ResponseEntity<Transaction> create(@AuthenticationPrincipal Long userId,
                                              @Valid @RequestBody TransactionRequest request) {
        Transaction saved = service.create(userId, request);
        return ResponseEntity.created(URI.create("/api/transactions/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public Transaction update(@AuthenticationPrincipal Long userId, @PathVariable Long id,
                              @Valid @RequestBody TransactionRequest request) {
        return service.update(userId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        service.delete(userId, id);
    }

    /** Upload the old console app's transactions.txt to migrate its data. */
    @PostMapping("/import")
    public Map<String, Integer> importLegacy(@AuthenticationPrincipal Long userId,
                                             @RequestParam("file") MultipartFile file) throws IOException {
        return Map.of("imported", service.importLegacy(userId, file.getInputStream()));
    }
}

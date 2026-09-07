package com.bankguard.banking.controller;

import com.bankguard.banking.dto.request.TransactionRequest;
import com.bankguard.banking.dto.response.TransactionResponse;
import com.bankguard.banking.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.deposit(username, request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.withdraw(username, request));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.transfer(username, request));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getMyTransactions(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(transactionService.getMyTransactions(username));
    }

    @GetMapping("/mini-statement")
    public ResponseEntity<List<TransactionResponse>> getMiniStatement(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(transactionService.getMiniStatement(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getMyTransaction(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(transactionService.getMyTransaction(id, username));
    }
}
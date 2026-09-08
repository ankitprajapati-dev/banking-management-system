package com.bankguard.banking.controller;

import java.util.List;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.bankguard.banking.dto.request.TransactionRequest;
import com.bankguard.banking.dto.response.TransactionResponse;
import com.bankguard.banking.service.TransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(
            TransactionService transactionService) {

        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        transactionService.deposit(
                                authentication.getName(),
                                request
                        )
                );
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        transactionService.withdraw(
                                authentication.getName(),
                                request
                        )
                );
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        transactionService.transfer(
                                authentication.getName(),
                                request
                        )
                );
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>>
            getMyTransactions(
                    Authentication authentication) {

        return ResponseEntity.ok(
                transactionService.getMyTransactions(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/mini-statement")
    public ResponseEntity<List<TransactionResponse>>
            getMiniStatement(
                    Authentication authentication) {

        return ResponseEntity.ok(
                transactionService.getMiniStatement(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse>
            getMyTransaction(
                    @PathVariable Long id,
                    Authentication authentication) {

        return ResponseEntity.ok(
                transactionService.getMyTransaction(
                        id,
                        authentication.getName()
                )
        );
    }
}
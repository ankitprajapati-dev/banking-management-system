package com.bankguard.banking.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.bankguard.banking.dto.request.AccountRequest;
import com.bankguard.banking.dto.response.AccountResponse;
import com.bankguard.banking.service.AccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(
            AccountService accountService) {

        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountRequest request,
            Authentication authentication) {

        String username =
                authentication.getName();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        accountService.createAccount(
                                username,
                                request
                        )
                );
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>>
            getMyAccounts(
                    Authentication authentication) {

        return ResponseEntity.ok(
                accountService.getMyAccounts(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getMyAccount(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                accountService.getMyAccount(
                        id,
                        authentication.getName()
                )
        );
    }

    @GetMapping("/balance/total")
    public ResponseEntity<BigDecimal> getTotalBalance(
            Authentication authentication) {

        return ResponseEntity.ok(
                accountService.getTotalBalance(
                        authentication.getName()
                )
        );
    }
}
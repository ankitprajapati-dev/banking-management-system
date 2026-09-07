package com.bankguard.banking.controller;

import com.bankguard.banking.dto.request.AccountRequest;
import com.bankguard.banking.dto.response.AccountResponse;
import com.bankguard.banking.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        AccountResponse response = accountService.createAccount(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getMyAccounts(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(accountService.getMyAccounts(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getMyAccount(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(accountService.getMyAccount(id, username));
    }

    @GetMapping("/balance/total")
    public ResponseEntity<BigDecimal> getTotalBalance(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(accountService.getTotalBalance(username));
    }
}
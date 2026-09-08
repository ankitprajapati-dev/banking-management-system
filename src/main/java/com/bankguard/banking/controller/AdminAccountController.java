package com.bankguard.banking.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bankguard.banking.dto.response.AccountResponse;
import com.bankguard.banking.service.AccountService;

@RestController
@RequestMapping("/admin/accounts")
public class AdminAccountController {

    private final AccountService accountService;

    public AdminAccountController(
            AccountService accountService) {

        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>>
            getAllAccounts() {

        return ResponseEntity.ok(
                accountService.getAllAccounts()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse>
            getAccountById(
                    @PathVariable Long id) {

        return ResponseEntity.ok(
                accountService.getAccountByIdForAdmin(id)
        );
    }

    @PutMapping("/{id}/block")
    public ResponseEntity<AccountResponse>
            blockAccount(
                    @PathVariable Long id) {

        return ResponseEntity.ok(
                accountService.blockAccount(id)
        );
    }

    @PutMapping("/{id}/unblock")
    public ResponseEntity<AccountResponse>
            unblockAccount(
                    @PathVariable Long id) {

        return ResponseEntity.ok(
                accountService.unblockAccount(id)
        );
    }
}
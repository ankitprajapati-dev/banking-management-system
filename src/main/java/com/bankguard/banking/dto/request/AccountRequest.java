package com.bankguard.banking.dto.request;

import com.bankguard.banking.entity.AccountType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountRequest {

    @NotNull(message = "Account type is required")
    private AccountType accountType;
}
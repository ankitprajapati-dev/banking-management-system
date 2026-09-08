package com.bankguard.banking.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bankguard.banking.entity.TransactionStatus;
import com.bankguard.banking.entity.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class TransactionResponse {

    private Long id;
    private String transactionReference;
    private BigDecimal amount;
    private TransactionType transactionType;
    private TransactionStatus status;

    private Long sourceAccountId;
    private String sourceAccountNumber;

    private Long destinationAccountId;
    private String destinationAccountNumber;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
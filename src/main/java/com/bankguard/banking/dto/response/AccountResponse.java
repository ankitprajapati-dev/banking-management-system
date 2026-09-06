package com.bankguard.banking.dto.response;

import com.bankguard.banking.entity.AccountStatus;
import com.bankguard.banking.entity.AccountType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountResponse {
	private Long id;
	private String accountNumber;
	private AccountType accountType;
	private BigDecimal balance;
	private AccountStatus status;
	private Long customerId;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;
}
package com.bankguard.banking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bankguard.banking.entity.Account;
import com.bankguard.banking.entity.AccountStatus;
import com.bankguard.banking.entity.AccountType;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByIdAndCustomerId(
            Long accountId,
            Long customerId
    );

    List<Account> findByCustomerId(Long customerId);

    List<Account> findByAccountTypeAndStatus(
            AccountType type,
            AccountStatus status
    );

    long countByCustomerId(Long customerId);

    boolean existsByAccountNumber(String accountNumber);
}
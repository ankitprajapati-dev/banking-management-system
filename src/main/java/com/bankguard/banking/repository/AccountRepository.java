package com.bankguard.banking.repository;

import com.bankguard.banking.entity.Account;
import com.bankguard.banking.entity.AccountStatus;
import com.bankguard.banking.entity.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
	Optional<Account> findByAccountNumber(String accountNumber);

	Optional<Account> findByIdAndCustomerId(Long accountId, Long customerId);

	List<Account> findByCustomerId(Long customerId);

	List<Account> findByCustomerIdAndStatus(Long customerId, AccountStatus status);

	List<Account> findByAccountTypeAndStatus(AccountType type, AccountStatus status);

	boolean existsByAccountNumber(String accountNumber);
}
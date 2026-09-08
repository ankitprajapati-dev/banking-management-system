package com.bankguard.banking.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankguard.banking.dto.request.AccountRequest;
import com.bankguard.banking.dto.response.AccountResponse;
import com.bankguard.banking.entity.Account;
import com.bankguard.banking.entity.AccountStatus;
import com.bankguard.banking.entity.Customer;
import com.bankguard.banking.exception.BusinessException;
import com.bankguard.banking.exception.ResourceNotFoundException;
import com.bankguard.banking.repository.AccountRepository;
import com.bankguard.banking.repository.CustomerRepository;

@Service
public class AccountService {

    private static final Logger log =
            LoggerFactory.getLogger(AccountService.class);

    private static final int MAX_ACCOUNTS_PER_CUSTOMER = 5;

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(
            AccountRepository accountRepository,
            CustomerRepository customerRepository) {

        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public AccountResponse createAccount(
            String username,
            AccountRequest request) {

        Customer customer = getCustomerByUsername(username);

        validateAccountLimit(customer.getId());

        Account account = new Account();

        account.setAccountNumber(generateAccountNumber());
        account.setAccountType(request.getAccountType());
        account.setBalance(BigDecimal.ZERO.setScale(2));
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer);

        Account savedAccount = accountRepository.save(account);

        log.info(
                "Account created for user {}: {}",
                username,
                savedAccount.getAccountNumber()
        );

        return mapToResponse(savedAccount);
    }

    private void validateAccountLimit(Long customerId) {

        long count = accountRepository.countByCustomerId(customerId);

        if (count >= MAX_ACCOUNTS_PER_CUSTOMER) {
            throw new BusinessException(
                    "Maximum 5 accounts allowed per customer"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts(String username) {

        Customer customer = getCustomerByUsername(username);

        return accountRepository
                .findByCustomerId(customer.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getMyAccount(
            Long accountId,
            String username) {

        Customer customer = getCustomerByUsername(username);

        Account account =
                accountRepository
                        .findByIdAndCustomerId(
                                accountId,
                                customer.getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Account not found"
                                )
                        );

        return mapToResponse(account);
    }

    @Transactional
    public AccountResponse blockAccount(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found"
                        )
                );

        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new BusinessException(
                    "Account is already blocked"
            );
        }

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException(
                    "Closed account cannot be blocked"
            );
        }

        account.setStatus(AccountStatus.BLOCKED);

        log.info(
                "Account blocked: {}",
                account.getAccountNumber()
        );

        return mapToResponse(account);
    }

    @Transactional
    public AccountResponse unblockAccount(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found"
                        )
                );

        if (account.getStatus() != AccountStatus.BLOCKED) {
            throw new BusinessException(
                    "Only blocked accounts can be unblocked"
            );
        }

        account.setStatus(AccountStatus.ACTIVE);

        log.info(
                "Account unblocked: {}",
                account.getAccountNumber()
        );

        return mapToResponse(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {

        return accountRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountByIdForAdmin(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found"
                        )
                );

        return mapToResponse(account);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalBalance(String username) {

        Customer customer = getCustomerByUsername(username);

        return accountRepository.findByCustomerId(customer.getId())
                .stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Customer getCustomerByUsername(String username) {

        return customerRepository
                .findByUserUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found"
                        )
                );
    }

    private String generateAccountNumber() {

        String accountNumber;

        do {
            accountNumber =
                    "BKG"
                    + UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 12)
                            .toUpperCase();

        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    private AccountResponse mapToResponse(Account account) {

        AccountResponse response = new AccountResponse();

        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountType(account.getAccountType());

        BigDecimal balance = account.getBalance() == null
                ? BigDecimal.ZERO
                : account.getBalance();

        response.setBalance(
                balance.setScale(2, RoundingMode.HALF_UP)
        );

        response.setStatus(account.getStatus());
        response.setCustomerId(account.getCustomer().getId());
        response.setCreatedAt(account.getCreatedAt());

        return response;
    }
}
package com.bankguard.banking.service;

import com.bankguard.banking.dto.request.AccountRequest;
import com.bankguard.banking.dto.response.AccountResponse;
import com.bankguard.banking.entity.Account;
import com.bankguard.banking.entity.AccountStatus;
import com.bankguard.banking.entity.Customer;
import com.bankguard.banking.exception.BusinessException;
import com.bankguard.banking.exception.ResourceNotFoundException;
import com.bankguard.banking.repository.AccountRepository;
import com.bankguard.banking.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository,
                          CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public AccountResponse createAccount(String username, AccountRequest request) {
        log.info("Creating account for user: {}", username);

        Customer customer = getCustomerByUsername(username);
        validateAccountLimit(customer.getId());

        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setAccountType(request.getAccountType());
        account.setBalance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());
        account.setCustomer(customer);

        Account saved = accountRepository.save(account);
        log.info("Account created: {}", saved.getAccountNumber());

        return mapToResponse(saved);
    }

    private void validateAccountLimit(Long customerId) {
        long count = accountRepository.findByCustomerId(customerId).size();
        if (count >= 5) {
            throw new BusinessException("Maximum 5 accounts allowed per customer");
        }
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts(String username) {
        Customer customer = getCustomerByUsername(username);
        return accountRepository.findByCustomerId(customer.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getMyAccount(Long accountId, String username) {
        Customer customer = getCustomerByUsername(username);
        Account account = accountRepository.findByIdAndCustomerId(accountId, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return mapToResponse(account);
    }

    @Transactional
    public AccountResponse blockAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new BusinessException("Account is already blocked");
        }
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException("Closed account cannot be blocked");
        }

        account.setStatus(AccountStatus.BLOCKED);
        log.info("Account blocked: {}", account.getAccountNumber());
        return mapToResponse(account);
    }

    @Transactional
    public AccountResponse unblockAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (account.getStatus() != AccountStatus.BLOCKED) {
            throw new BusinessException("Only blocked accounts can be unblocked");
        }

        account.setStatus(AccountStatus.ACTIVE);
        log.info("Account unblocked: {}", account.getAccountNumber());
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
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return mapToResponse(account);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalBalance(String username) {
        Customer customer = getCustomerByUsername(username);
        return accountRepository.findByCustomerId(customer.getId())
                .stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public long getAccountCount(String username) {
        Customer customer = getCustomerByUsername(username);
        return accountRepository.findByCustomerId(customer.getId()).size();
    }

    private Customer getCustomerByUsername(String username) {
        return customerRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    private String generateAccountNumber() {
        return "BKG" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private AccountResponse mapToResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountType(account.getAccountType());
        response.setBalance(account.getBalance());
        response.setStatus(account.getStatus());
        response.setCreatedAt(account.getCreatedAt());
        response.setCustomerId(account.getCustomer().getId());
        return response;
    }
}
package com.bankguard.banking.service;

import com.bankguard.banking.dto.request.TransactionRequest;
import com.bankguard.banking.dto.response.TransactionResponse;
import com.bankguard.banking.entity.*;
import com.bankguard.banking.exception.BusinessException;
import com.bankguard.banking.exception.ResourceNotFoundException;
import com.bankguard.banking.repository.AccountRepository;
import com.bankguard.banking.repository.BankTransactionRepository;
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
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private static final BigDecimal MINIMUM_BALANCE = new BigDecimal("500.00");
    private static final BigDecimal DAILY_TRANSFER_LIMIT = new BigDecimal("100000.00");

    private final AccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;

    public TransactionService(AccountRepository accountRepository,
                              BankTransactionRepository transactionRepository,
                              CustomerRepository customerRepository,
                              NotificationService notificationService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public TransactionResponse deposit(String username, TransactionRequest request) {
        log.info("Processing deposit for user: {}", username);

        Customer customer = getCustomerByUsername(username);
        Account account = validateAccountOwnership(request.getAccountId(), customer);
        validateActiveAccount(account, "Only active accounts can receive deposits");

        account.setBalance(account.getBalance().add(request.getAmount()));

        BankTransaction transaction = createTransaction(
                null,
                account,
                request.getAmount(),
                TransactionType.DEPOSIT,
                TransactionStatus.COMPLETED,
                request.getDescription()
        );

        return mapToResponse(transaction);
    }

    @Transactional
    public TransactionResponse withdraw(String username, TransactionRequest request) {
        log.info("Processing withdrawal for user: {}", username);

        Customer customer = getCustomerByUsername(username);
        Account account = validateAccountOwnership(request.getAccountId(), customer);
        validateActiveAccount(account, "Only active accounts can be used for withdrawal");

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException("Insufficient balance");
        }

        if (account.getAccountType() == AccountType.SAVINGS) {
            BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
            if (newBalance.compareTo(MINIMUM_BALANCE) < 0) {
                throw new BusinessException("Minimum balance of ₹" + MINIMUM_BALANCE +
                        " required for Savings account");
            }
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));

        BankTransaction transaction = createTransaction(
                account,
                null,
                request.getAmount(),
                TransactionType.WITHDRAWAL,
                TransactionStatus.COMPLETED,
                request.getDescription()
        );

        return mapToResponse(transaction);
    }

    @Transactional
    public TransactionResponse transfer(String username, TransactionRequest request) {
        log.info("Processing transfer for user: {}", username);

        Customer customer = getCustomerByUsername(username);

        Account sourceAccount = validateAccountOwnership(request.getAccountId(), customer);
        validateActiveAccount(sourceAccount, "Source account is not active");

        if (request.getDestinationAccountId() == null) {
            throw new BusinessException("Destination account is required");
        }

        Account destinationAccount = accountRepository.findById(request.getDestinationAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));
        validateActiveAccount(destinationAccount, "Destination account is not active");

        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new BusinessException("Cannot transfer to same account");
        }

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException("Insufficient balance");
        }

        validateDailyLimit(customer.getId(), request.getAmount());

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));

        BankTransaction transaction = createTransaction(
                sourceAccount,
                destinationAccount,
                request.getAmount(),
                TransactionType.TRANSFER,
                TransactionStatus.COMPLETED,
                request.getDescription()
        );

        return mapToResponse(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyTransactions(String username) {
        Customer customer = getCustomerByUsername(username);
        log.info("Fetching all transactions for customer: {}", customer.getId());
        
        List<BankTransaction> transactions = transactionRepository
            .findTransactionsByCustomerId(customer.getId());
        
        log.info("Found {} transactions", transactions.size());
        return transactions.stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getMiniStatement(String username) {
        Customer customer = getCustomerByUsername(username);
        return transactionRepository.findLast10TransactionsByCustomerId(customer.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getMyTransaction(Long transactionId, String username) {
        Customer customer = getCustomerByUsername(username);
        BankTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        boolean belongsToCustomer = false;
        if (transaction.getSourceAccount() != null &&
                transaction.getSourceAccount().getCustomer().getId().equals(customer.getId())) {
            belongsToCustomer = true;
        }
        if (transaction.getDestinationAccount() != null &&
                transaction.getDestinationAccount().getCustomer().getId().equals(customer.getId())) {
            belongsToCustomer = true;
        }

        if (!belongsToCustomer) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        return mapToResponse(transaction);
    }

    private void validateDailyLimit(Long customerId, BigDecimal amount) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        List<BankTransaction> todayTransactions = transactionRepository
                .findTransactionsByCustomerIdAndDateRange(customerId, startOfDay, LocalDateTime.now());

        BigDecimal todayTotal = todayTransactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.TRANSFER)
                .map(BankTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (todayTotal.add(amount).compareTo(DAILY_TRANSFER_LIMIT) > 0) {
            throw new BusinessException("Daily transfer limit of ₹" + DAILY_TRANSFER_LIMIT + " exceeded");
        }
    }

    private Customer getCustomerByUsername(String username) {
        return customerRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    private Account validateAccountOwnership(Long accountId, Customer customer) {
        return accountRepository.findByIdAndCustomerId(accountId, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private void validateActiveAccount(Account account, String message) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(message);
        }
    }

    private BankTransaction createTransaction(Account source, Account destination,
                                              BigDecimal amount, TransactionType type,
                                              TransactionStatus status, String description) {
        BankTransaction transaction = new BankTransaction();
        transaction.setTransactionReference("TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        transaction.setAmount(amount);
        transaction.setTransactionType(type);
        transaction.setStatus(status);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setSourceAccount(source);
        transaction.setDestinationAccount(destination);
        transaction.setDescription(description);

        return transactionRepository.save(transaction);
    }

    private TransactionResponse mapToResponse(BankTransaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setTransactionReference(transaction.getTransactionReference());
        response.setAmount(transaction.getAmount());
        response.setTransactionType(transaction.getTransactionType());
        response.setStatus(transaction.getStatus());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setDescription(transaction.getDescription());

        if (transaction.getSourceAccount() != null) {
            response.setSourceAccountId(transaction.getSourceAccount().getId());
            response.setSourceAccountNumber(transaction.getSourceAccount().getAccountNumber());
        }
        if (transaction.getDestinationAccount() != null) {
            response.setDestinationAccountId(transaction.getDestinationAccount().getId());
            response.setDestinationAccountNumber(transaction.getDestinationAccount().getAccountNumber());
        }

        return response;
    }
}
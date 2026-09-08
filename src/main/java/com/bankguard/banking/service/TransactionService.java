package com.bankguard.banking.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankguard.banking.dto.request.TransactionRequest;
import com.bankguard.banking.dto.response.TransactionResponse;
import com.bankguard.banking.entity.Account;
import com.bankguard.banking.entity.AccountStatus;
import com.bankguard.banking.entity.AccountType;
import com.bankguard.banking.entity.BankTransaction;
import com.bankguard.banking.entity.Customer;
import com.bankguard.banking.entity.TransactionStatus;
import com.bankguard.banking.entity.TransactionType;
import com.bankguard.banking.exception.BusinessException;
import com.bankguard.banking.exception.ResourceNotFoundException;
import com.bankguard.banking.repository.AccountRepository;
import com.bankguard.banking.repository.BankTransactionRepository;
import com.bankguard.banking.repository.CustomerRepository;

@Service
public class TransactionService {

    private static final BigDecimal MINIMUM_SAVINGS_BALANCE =
            new BigDecimal("500.00");

    private static final BigDecimal DAILY_TRANSFER_LIMIT =
            new BigDecimal("100000.00");

    private final AccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;

    public TransactionService(
            AccountRepository accountRepository,
            BankTransactionRepository transactionRepository,
            CustomerRepository customerRepository) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public TransactionResponse deposit(
            String username,
            TransactionRequest request) {

        validateNoDestination(request);

        Customer customer = getCustomerByUsername(username);

        Account account =
                validateAccountOwnership(
                        request.getAccountId(),
                        customer
                );

        validateActiveAccount(
                account,
                "Only active accounts can receive deposits"
        );

        account.setBalance(
                account.getBalance()
                        .add(request.getAmount())
                        .setScale(2, RoundingMode.HALF_UP)
        );

        BankTransaction transaction =
                createTransaction(
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
    public TransactionResponse withdraw(
            String username,
            TransactionRequest request) {

        validateNoDestination(request);

        Customer customer = getCustomerByUsername(username);

        Account account =
                validateAccountOwnership(
                        request.getAccountId(),
                        customer
                );

        validateActiveAccount(
                account,
                "Only active accounts can be used for withdrawal"
        );

        if (account.getBalance()
                .compareTo(request.getAmount()) < 0) {

            throw new BusinessException(
                    "Insufficient balance"
            );
        }

        BigDecimal newBalance =
                account.getBalance()
                        .subtract(request.getAmount());

        if (account.getAccountType() == AccountType.SAVINGS
                && newBalance.compareTo(
                        MINIMUM_SAVINGS_BALANCE
                   ) < 0) {

            throw new BusinessException(
                    "Minimum balance of ₹"
                    + MINIMUM_SAVINGS_BALANCE
                    + " required for Savings account"
            );
        }

        account.setBalance(
                newBalance.setScale(
                        2,
                        RoundingMode.HALF_UP
                )
        );

        BankTransaction transaction =
                createTransaction(
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
    public TransactionResponse transfer(
            String username,
            TransactionRequest request) {

        if (request.getDestinationAccountId() == null) {
            throw new BusinessException(
                    "Destination account is required"
            );
        }

        Customer customer = getCustomerByUsername(username);

        Account sourceAccount =
                validateAccountOwnership(
                        request.getAccountId(),
                        customer
                );

        validateActiveAccount(
                sourceAccount,
                "Source account is not active"
        );

        Account destinationAccount =
                accountRepository
                        .findById(
                                request.getDestinationAccountId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Destination account not found"
                                )
                        );

        validateActiveAccount(
                destinationAccount,
                "Destination account is not active"
        );

        if (sourceAccount.getId()
                .equals(destinationAccount.getId())) {

            throw new BusinessException(
                    "Cannot transfer to same account"
            );
        }

        if (sourceAccount.getBalance()
                .compareTo(request.getAmount()) < 0) {

            throw new BusinessException(
                    "Insufficient balance"
            );
        }

        validateDailyTransferLimit(
                customer.getId(),
                request.getAmount()
        );

        sourceAccount.setBalance(
                sourceAccount.getBalance()
                        .subtract(request.getAmount())
                        .setScale(2, RoundingMode.HALF_UP)
        );

        destinationAccount.setBalance(
                destinationAccount.getBalance()
                        .add(request.getAmount())
                        .setScale(2, RoundingMode.HALF_UP)
        );

        BankTransaction transaction =
                createTransaction(
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
    public List<TransactionResponse> getMyTransactions(
            String username) {

        Customer customer = getCustomerByUsername(username);

        return transactionRepository
                .findTransactionsByCustomerId(customer.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getMiniStatement(
            String username) {

        Customer customer = getCustomerByUsername(username);

        return transactionRepository
                .findLastTransactionsByCustomerId(
                        customer.getId()
                )
                .stream()
                .limit(10)
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getMyTransaction(
            Long transactionId,
            String username) {

        Customer customer = getCustomerByUsername(username);

        BankTransaction transaction =
                transactionRepository
                        .findById(transactionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Transaction not found"
                                )
                        );

        boolean belongsToCustomer = false;

        if (transaction.getSourceAccount() != null
                && transaction.getSourceAccount()
                        .getCustomer()
                        .getId()
                        .equals(customer.getId())) {

            belongsToCustomer = true;
        }

        if (transaction.getDestinationAccount() != null
                && transaction.getDestinationAccount()
                        .getCustomer()
                        .getId()
                        .equals(customer.getId())) {

            belongsToCustomer = true;
        }

        if (!belongsToCustomer) {
            throw new ResourceNotFoundException(
                    "Transaction not found"
            );
        }

        return mapToResponse(transaction);
    }

    private void validateDailyTransferLimit(
            Long customerId,
            BigDecimal amount) {

        LocalDateTime startOfDay =
                LocalDate.now().atStartOfDay();

        LocalDateTime endOfDay =
                LocalDate.now().plusDays(1).atStartOfDay()
                        .minusNanos(1);

        List<BankTransaction> todayTransfers =
                transactionRepository
                        .findOutgoingTransactionsByCustomerIdAndDateRange(
                                customerId,
                                TransactionType.TRANSFER,
                                TransactionStatus.COMPLETED,
                                startOfDay,
                                endOfDay
                        );

        BigDecimal todayTotal =
                todayTransfers.stream()
                        .map(BankTransaction::getAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        if (todayTotal.add(amount)
                .compareTo(DAILY_TRANSFER_LIMIT) > 0) {

            throw new BusinessException(
                    "Daily transfer limit of ₹"
                    + DAILY_TRANSFER_LIMIT
                    + " exceeded"
            );
        }
    }

    private void validateNoDestination(
            TransactionRequest request) {

        if (request.getDestinationAccountId() != null) {
            throw new BusinessException(
                    "Destination account is not allowed for this transaction"
            );
        }
    }

    private Customer getCustomerByUsername(
            String username) {

        return customerRepository
                .findByUserUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found"
                        )
                );
    }

    private Account validateAccountOwnership(
            Long accountId,
            Customer customer) {

        return accountRepository
                .findByIdAndCustomerId(
                        accountId,
                        customer.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found"
                        )
                );
    }

    private void validateActiveAccount(
            Account account,
            String message) {

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(message);
        }
    }

    private BankTransaction createTransaction(
            Account source,
            Account destination,
            BigDecimal amount,
            TransactionType type,
            TransactionStatus status,
            String description) {

        BankTransaction transaction =
                new BankTransaction();

        transaction.setTransactionReference(
                generateTransactionReference()
        );

        transaction.setAmount(
                amount.setScale(2, RoundingMode.HALF_UP)
        );

        transaction.setTransactionType(type);
        transaction.setStatus(status);
        transaction.setSourceAccount(source);
        transaction.setDestinationAccount(destination);
        transaction.setDescription(description);

        return transactionRepository.save(transaction);
    }

    private String generateTransactionReference() {

        String reference;

        do {
            reference =
                    "TXN"
                    + UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 12)
                            .toUpperCase();

        } while (
                transactionRepository
                        .existsByTransactionReference(reference)
        );

        return reference;
    }

    private TransactionResponse mapToResponse(
            BankTransaction transaction) {

        TransactionResponse response =
                new TransactionResponse();

        response.setId(transaction.getId());

        response.setTransactionReference(
                transaction.getTransactionReference()
        );

        response.setAmount(
                transaction.getAmount()
                        .setScale(2, RoundingMode.HALF_UP)
        );

        response.setTransactionType(
                transaction.getTransactionType()
        );

        response.setStatus(transaction.getStatus());

        response.setDescription(
                transaction.getDescription()
        );

        response.setCreatedAt(
                transaction.getCreatedAt()
        );

        if (transaction.getSourceAccount() != null) {

            response.setSourceAccountId(
                    transaction.getSourceAccount().getId()
            );

            response.setSourceAccountNumber(
                    transaction.getSourceAccount().getAccountNumber()
            );
        }

        if (transaction.getDestinationAccount() != null) {

            response.setDestinationAccountId(
                    transaction.getDestinationAccount().getId()
            );

            response.setDestinationAccountNumber(
                    transaction.getDestinationAccount()
                            .getAccountNumber()
            );
        }

        return response;
    }
}
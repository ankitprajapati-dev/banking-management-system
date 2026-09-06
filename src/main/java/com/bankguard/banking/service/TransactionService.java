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

	public TransactionService(AccountRepository accountRepository, BankTransactionRepository transactionRepository,
			CustomerRepository customerRepository, NotificationService notificationService) {
		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
		this.customerRepository = customerRepository;
		this.notificationService = notificationService;
	}

	// =========================================================
	// DEPOSIT
	// =========================================================

	@Transactional
	public TransactionResponse deposit(String username, TransactionRequest request) {
		log.info("💰 Processing deposit for user: {}", username);

		Customer customer = getCustomerByUsername(username);
		Account account = validateAccountOwnership(request.getAccountId(), customer);
		validateActiveAccount(account, "Only active accounts can receive deposits");

		// Update balance
		account.setBalance(account.getBalance().add(request.getAmount()));

		// Create transaction with description
		BankTransaction transaction = createTransaction(null, account, request.getAmount(), TransactionType.DEPOSIT,
				TransactionStatus.COMPLETED, request.getDescription());

		// Send notification
		notificationService.sendTransactionAlert(customer.getEmail(), customer.getFullName(),
				mapToResponse(transaction));

		log.info("✅ Deposit completed: ₹{} → {}", request.getAmount(), account.getAccountNumber());
		return mapToResponse(transaction);
	}

	// =========================================================
	// WITHDRAWAL
	// =========================================================

	@Transactional
	public TransactionResponse withdraw(String username, TransactionRequest request) {
		log.info("💰 Processing withdrawal for user: {}", username);

		Customer customer = getCustomerByUsername(username);
		Account account = validateAccountOwnership(request.getAccountId(), customer);
		validateActiveAccount(account, "Only active accounts can be used for withdrawal");

		// Check sufficient balance
		if (account.getBalance().compareTo(request.getAmount()) < 0) {
			throw new BusinessException("Insufficient balance");
		}

		// Check minimum balance for savings account
		if (account.getAccountType() == AccountType.SAVINGS) {
			BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
			if (newBalance.compareTo(MINIMUM_BALANCE) < 0) {
				throw new BusinessException("Minimum balance of ₹" + MINIMUM_BALANCE + " required for Savings account");
			}
		}

		// Update balance
		account.setBalance(account.getBalance().subtract(request.getAmount()));

		// Create transaction with description
		BankTransaction transaction = createTransaction(account, null, request.getAmount(), TransactionType.WITHDRAWAL,
				TransactionStatus.COMPLETED, request.getDescription());

		// Send notification
		notificationService.sendTransactionAlert(customer.getEmail(), customer.getFullName(),
				mapToResponse(transaction));

		log.info("✅ Withdrawal completed: ₹{} from {}", request.getAmount(), account.getAccountNumber());
		return mapToResponse(transaction);
	}

	// =========================================================
	// TRANSFER
	// =========================================================

	@Transactional
	public TransactionResponse transfer(String username, TransactionRequest request) {
		log.info("💰 Processing transfer for user: {}", username);

		Customer customer = getCustomerByUsername(username);

		// Validate source account
		Account sourceAccount = validateAccountOwnership(request.getAccountId(), customer);
		validateActiveAccount(sourceAccount, "Source account is not active");

		// Validate destination account
		if (request.getDestinationAccountId() == null) {
			throw new BusinessException("Destination account is required");
		}

		Account destinationAccount = accountRepository.findById(request.getDestinationAccountId())
				.orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));
		validateActiveAccount(destinationAccount, "Destination account is not active");

		// Prevent self-transfer
		if (sourceAccount.getId().equals(destinationAccount.getId())) {
			throw new BusinessException("Cannot transfer to same account");
		}

		// Check sufficient balance
		if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
			throw new BusinessException("Insufficient balance");
		}

		// Check daily limit
		validateDailyLimit(customer.getId(), request.getAmount());

		// Perform transfer
		sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
		destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));

		// Create transaction with description
		BankTransaction transaction = createTransaction(sourceAccount, destinationAccount, request.getAmount(),
				TransactionType.TRANSFER, TransactionStatus.COMPLETED, request.getDescription());

		// Send notifications
		TransactionResponse response = mapToResponse(transaction);
		notificationService.sendTransactionAlert(customer.getEmail(), customer.getFullName(), response);

		// Notify destination account owner (if different customer)
		Customer destCustomer = destinationAccount.getCustomer();
		if (!destCustomer.getId().equals(customer.getId())) {
			notificationService.sendTransactionAlert(destCustomer.getEmail(), destCustomer.getFullName(), response);
		}

		log.info("✅ Transfer completed: {} → {}", sourceAccount.getAccountNumber(),
				destinationAccount.getAccountNumber());
		return response;
	}

	// =========================================================
	// GET TRANSACTIONS
	// =========================================================

	@Transactional(readOnly = true)
	public List<TransactionResponse> getMyTransactions(String username) {
		Customer customer = getCustomerByUsername(username);
		return transactionRepository.findTransactionsByCustomerId(customer.getId()).stream().map(this::mapToResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<TransactionResponse> getMiniStatement(String username) {
		Customer customer = getCustomerByUsername(username);
		return transactionRepository.findLast10TransactionsByCustomerId(customer.getId()).stream()
				.map(this::mapToResponse).toList();
	}

	@Transactional(readOnly = true)
	public TransactionResponse getMyTransaction(Long transactionId, String username) {
		Customer customer = getCustomerByUsername(username);
		BankTransaction transaction = transactionRepository.findById(transactionId)
				.orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

		boolean belongsToCustomer = false;
		if (transaction.getSourceAccount() != null
				&& transaction.getSourceAccount().getCustomer().getId().equals(customer.getId())) {
			belongsToCustomer = true;
		}
		if (transaction.getDestinationAccount() != null
				&& transaction.getDestinationAccount().getCustomer().getId().equals(customer.getId())) {
			belongsToCustomer = true;
		}

		if (!belongsToCustomer) {
			throw new ResourceNotFoundException("Transaction not found");
		}

		return mapToResponse(transaction);
	}

	// =========================================================
	// VALIDATION HELPERS
	// =========================================================

	private void validateDailyLimit(Long customerId, BigDecimal amount) {
		LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
		List<BankTransaction> todayTransactions = transactionRepository
				.findTransactionsByCustomerIdAndDateRange(customerId, startOfDay, LocalDateTime.now());

		BigDecimal todayTotal = todayTransactions.stream()
				.filter(t -> t.getTransactionType() == TransactionType.TRANSFER).map(BankTransaction::getAmount)
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

	// =========================================================
	// TRANSACTION CREATION
	// =========================================================

	private BankTransaction createTransaction(Account source, Account destination, BigDecimal amount,
			TransactionType type, TransactionStatus status, String description) {
		BankTransaction transaction = new BankTransaction();
		transaction.setTransactionReference(
				"TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
		transaction.setAmount(amount);
		transaction.setTransactionType(type);
		transaction.setStatus(status);
		transaction.setCreatedAt(LocalDateTime.now());
		transaction.setSourceAccount(source);
		transaction.setDestinationAccount(destination);

		// ✅ YEH LINE IMPORTANT HAI - Description set karna
		transaction.setDescription(description);

		return transactionRepository.save(transaction);
	}

	// =========================================================
	// MAP TO RESPONSE
	// =========================================================

	private TransactionResponse mapToResponse(BankTransaction transaction) {
		TransactionResponse response = new TransactionResponse();
		response.setId(transaction.getId());
		response.setTransactionReference(transaction.getTransactionReference());
		response.setAmount(transaction.getAmount());
		response.setTransactionType(transaction.getTransactionType());
		response.setStatus(transaction.getStatus());
		response.setCreatedAt(transaction.getCreatedAt());

		// ✅ YEH LINE IMPORTANT HAI - Description set karna
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
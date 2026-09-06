package com.bankguard.banking.repository;

import com.bankguard.banking.entity.BankTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
   
	Optional<BankTransaction> findByTransactionReference(String reference);

    @Query("SELECT t FROM BankTransaction t WHERE " +
           "t.sourceAccount.customer.id = :customerId OR " +
           "t.destinationAccount.customer.id = :customerId " +
           "ORDER BY t.createdAt DESC")
    List<BankTransaction> findTransactionsByCustomerId(@Param("customerId") Long customerId);

    @Query(value = "SELECT * FROM bank_transactions WHERE " +
           "source_account_id IN (SELECT id FROM accounts WHERE customer_id = :customerId) OR " +
           "destination_account_id IN (SELECT id FROM accounts WHERE customer_id = :customerId) " +
           "ORDER BY created_at DESC LIMIT 10", nativeQuery = true)
    List<BankTransaction> findLast10TransactionsByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT t FROM BankTransaction t WHERE " +
           "(t.sourceAccount.customer.id = :customerId OR " +
           "t.destinationAccount.customer.id = :customerId) " +
           "AND t.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY t.createdAt DESC")
    List<BankTransaction> findTransactionsByCustomerIdAndDateRange(
            @Param("customerId") Long customerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    Page<BankTransaction> findBySourceAccountCustomerIdOrDestinationAccountCustomerId(
            Long sourceCustomerId, Long destinationCustomerId, Pageable pageable);

    boolean existsByTransactionReference(String reference);
}
package com.bankguard.banking.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bankguard.banking.entity.BankTransaction;
import com.bankguard.banking.entity.TransactionStatus;
import com.bankguard.banking.entity.TransactionType;

public interface BankTransactionRepository
        extends JpaRepository<BankTransaction, Long> {

    Optional<BankTransaction> findByTransactionReference(
            String reference
    );

    @Query("""
        SELECT t
        FROM BankTransaction t
        LEFT JOIN t.sourceAccount sa
        LEFT JOIN t.destinationAccount da
        WHERE sa.customer.id = :customerId
           OR da.customer.id = :customerId
        ORDER BY t.createdAt DESC
        """)
    List<BankTransaction> findTransactionsByCustomerId(
            @Param("customerId") Long customerId
    );

    @Query("""
        SELECT t
        FROM BankTransaction t
        LEFT JOIN t.sourceAccount sa
        LEFT JOIN t.destinationAccount da
        WHERE (sa.customer.id = :customerId
           OR da.customer.id = :customerId)
        ORDER BY t.createdAt DESC
        """)
    List<BankTransaction> findLastTransactionsByCustomerId(
            @Param("customerId") Long customerId
    );

    @Query("""
        SELECT t
        FROM BankTransaction t
        WHERE t.sourceAccount.customer.id = :customerId
          AND t.transactionType = :type
          AND t.status = :status
          AND t.createdAt BETWEEN :startDate AND :endDate
        ORDER BY t.createdAt DESC
        """)
    List<BankTransaction> findOutgoingTransactionsByCustomerIdAndDateRange(
            @Param("customerId") Long customerId,
            @Param("type") TransactionType type,
            @Param("status") TransactionStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    boolean existsByTransactionReference(String reference);
}
package com.bankguard.banking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bankguard.banking.entity.Beneficiary;
import com.bankguard.banking.entity.BeneficiaryStatus;

public interface BeneficiaryRepository
        extends JpaRepository<Beneficiary, Long> {

    List<Beneficiary> findByCustomerIdAndStatus(
            Long customerId,
            BeneficiaryStatus status
    );

    Optional<Beneficiary> findByIdAndCustomerIdAndStatus(
            Long id,
            Long customerId,
            BeneficiaryStatus status
    );

    boolean existsByCustomerIdAndBeneficiaryAccountNumber(
            Long customerId,
            String accountNumber
    );
}
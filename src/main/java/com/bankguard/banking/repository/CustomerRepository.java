package com.bankguard.banking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bankguard.banking.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByUserUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}
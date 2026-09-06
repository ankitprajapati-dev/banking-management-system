package com.bankguard.banking.service;

import com.bankguard.banking.dto.request.RegisterRequest;
import com.bankguard.banking.entity.Customer;
import com.bankguard.banking.entity.Role;
import com.bankguard.banking.entity.User;
import com.bankguard.banking.exception.BusinessException;
import com.bankguard.banking.repository.CustomerRepository;
import com.bankguard.banking.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       CustomerRepository customerRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(RegisterRequest request) {
        // Validate unique constraints
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists");
        }
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }
        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("Phone number already exists");
        }

        // Create User
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER);
        User savedUser = userRepository.save(user);

        // Create Customer
        Customer customer = new Customer();
        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUser(savedUser);
        customerRepository.save(customer);
    }
}
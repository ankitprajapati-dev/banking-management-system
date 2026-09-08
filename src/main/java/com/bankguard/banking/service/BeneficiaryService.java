package com.bankguard.banking.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankguard.banking.dto.request.BeneficiaryRequest;
import com.bankguard.banking.dto.response.BeneficiaryResponse;
import com.bankguard.banking.entity.Beneficiary;
import com.bankguard.banking.entity.BeneficiaryStatus;
import com.bankguard.banking.entity.Customer;
import com.bankguard.banking.exception.BusinessException;
import com.bankguard.banking.exception.ResourceNotFoundException;
import com.bankguard.banking.repository.BeneficiaryRepository;
import com.bankguard.banking.repository.CustomerRepository;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final CustomerRepository customerRepository;

    public BeneficiaryService(
            BeneficiaryRepository beneficiaryRepository,
            CustomerRepository customerRepository) {

        this.beneficiaryRepository = beneficiaryRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public BeneficiaryResponse createBeneficiary(
            String username,
            BeneficiaryRequest request) {

        Customer customer = getCustomerByUsername(username);

        validateDuplicate(
                customer.getId(),
                request.getBeneficiaryAccountNumber()
        );

        Beneficiary beneficiary = new Beneficiary();

        beneficiary.setNickname(request.getNickname());
        beneficiary.setBeneficiaryAccountNumber(
                request.getBeneficiaryAccountNumber()
        );
        beneficiary.setBankName(request.getBankName());
        beneficiary.setIfscCode(request.getIfscCode());
        beneficiary.setStatus(BeneficiaryStatus.ACTIVE);
        beneficiary.setCustomer(customer);

        Beneficiary saved = beneficiaryRepository.save(beneficiary);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> getMyBeneficiaries(
            String username) {

        Customer customer = getCustomerByUsername(username);

        return beneficiaryRepository
                .findByCustomerIdAndStatus(
                        customer.getId(),
                        BeneficiaryStatus.ACTIVE
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BeneficiaryResponse getMyBeneficiary(
            Long beneficiaryId,
            String username) {

        Customer customer = getCustomerByUsername(username);

        Beneficiary beneficiary =
                beneficiaryRepository
                        .findByIdAndCustomerIdAndStatus(
                                beneficiaryId,
                                customer.getId(),
                                BeneficiaryStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Beneficiary not found"
                                )
                        );

        return mapToResponse(beneficiary);
    }

    @Transactional
    public BeneficiaryResponse updateBeneficiary(
            Long beneficiaryId,
            String username,
            BeneficiaryRequest request) {

        Customer customer = getCustomerByUsername(username);

        Beneficiary beneficiary =
                beneficiaryRepository
                        .findByIdAndCustomerIdAndStatus(
                                beneficiaryId,
                                customer.getId(),
                                BeneficiaryStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Beneficiary not found"
                                )
                        );

        String newAccountNumber =
                request.getBeneficiaryAccountNumber();

        boolean accountNumberChanged =
                !beneficiary.getBeneficiaryAccountNumber()
                        .equals(newAccountNumber);

        if (accountNumberChanged) {
            validateDuplicate(
                    customer.getId(),
                    newAccountNumber
            );
        }

        beneficiary.setNickname(request.getNickname());
        beneficiary.setBeneficiaryAccountNumber(newAccountNumber);
        beneficiary.setBankName(request.getBankName());
        beneficiary.setIfscCode(request.getIfscCode());

        return mapToResponse(beneficiary);
    }

    @Transactional
    public void deleteBeneficiary(
            Long beneficiaryId,
            String username) {

        Customer customer = getCustomerByUsername(username);

        Beneficiary beneficiary =
                beneficiaryRepository
                        .findByIdAndCustomerIdAndStatus(
                                beneficiaryId,
                                customer.getId(),
                                BeneficiaryStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Beneficiary not found"
                                )
                        );

        beneficiary.setStatus(BeneficiaryStatus.BLOCKED);
    }

    private void validateDuplicate(
            Long customerId,
            String accountNumber) {

        if (beneficiaryRepository
                .existsByCustomerIdAndBeneficiaryAccountNumber(
                        customerId,
                        accountNumber
                )) {

            throw new BusinessException(
                    "Beneficiary already exists"
            );
        }
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

    private BeneficiaryResponse mapToResponse(
            Beneficiary beneficiary) {

        BeneficiaryResponse response =
                new BeneficiaryResponse();

        response.setId(beneficiary.getId());
        response.setNickname(beneficiary.getNickname());
        response.setBeneficiaryAccountNumber(
                beneficiary.getBeneficiaryAccountNumber()
        );
        response.setBankName(beneficiary.getBankName());
        response.setIfscCode(beneficiary.getIfscCode());
        response.setStatus(beneficiary.getStatus());

        return response;
    }
}
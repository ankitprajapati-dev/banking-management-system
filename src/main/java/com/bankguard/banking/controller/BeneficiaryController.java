package com.bankguard.banking.controller;

import java.util.List;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.bankguard.banking.dto.request.BeneficiaryRequest;
import com.bankguard.banking.dto.response.BeneficiaryResponse;
import com.bankguard.banking.service.BeneficiaryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(
            BeneficiaryService beneficiaryService) {

        this.beneficiaryService = beneficiaryService;
    }

    @PostMapping
    public ResponseEntity<BeneficiaryResponse>
            createBeneficiary(
                    @Valid @RequestBody BeneficiaryRequest request,
                    Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        beneficiaryService.createBeneficiary(
                                authentication.getName(),
                                request
                        )
                );
    }

    @GetMapping
    public ResponseEntity<List<BeneficiaryResponse>>
            getMyBeneficiaries(
                    Authentication authentication) {

        return ResponseEntity.ok(
                beneficiaryService.getMyBeneficiaries(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<BeneficiaryResponse>
            getMyBeneficiary(
                    @PathVariable Long id,
                    Authentication authentication) {

        return ResponseEntity.ok(
                beneficiaryService.getMyBeneficiary(
                        id,
                        authentication.getName()
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<BeneficiaryResponse>
            updateBeneficiary(
                    @PathVariable Long id,
                    @Valid @RequestBody BeneficiaryRequest request,
                    Authentication authentication) {

        return ResponseEntity.ok(
                beneficiaryService.updateBeneficiary(
                        id,
                        authentication.getName(),
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String>
            deleteBeneficiary(
                    @PathVariable Long id,
                    Authentication authentication) {

        beneficiaryService.deleteBeneficiary(
                id,
                authentication.getName()
        );

        return ResponseEntity.ok(
                "Beneficiary deleted successfully"
        );
    }
}
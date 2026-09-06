package com.bankguard.banking.controller;

import com.bankguard.banking.dto.request.BeneficiaryRequest;
import com.bankguard.banking.dto.response.BeneficiaryResponse;
import com.bankguard.banking.service.BeneficiaryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {

	private final BeneficiaryService beneficiaryService;

	public BeneficiaryController(BeneficiaryService beneficiaryService) {
		this.beneficiaryService = beneficiaryService;
	}

	@PostMapping
	public ResponseEntity<BeneficiaryResponse> createBeneficiary(@Valid @RequestBody BeneficiaryRequest request,
			Authentication authentication) {
		String username = authentication.getName();
		return ResponseEntity.status(HttpStatus.CREATED).body(beneficiaryService.createBeneficiary(username, request));
	}

	@GetMapping
	public ResponseEntity<List<BeneficiaryResponse>> getMyBeneficiaries(Authentication authentication) {
		String username = authentication.getName();
		return ResponseEntity.ok(beneficiaryService.getMyBeneficiaries(username));
	}

	@GetMapping("/{id}")
	public ResponseEntity<BeneficiaryResponse> getMyBeneficiary(@PathVariable Long id, Authentication authentication) {
		String username = authentication.getName();
		return ResponseEntity.ok(beneficiaryService.getMyBeneficiary(id, username));
	}

	@PutMapping("/{id}")
	public ResponseEntity<BeneficiaryResponse> updateBeneficiary(@PathVariable Long id,
			@Valid @RequestBody BeneficiaryRequest request, Authentication authentication) {
		String username = authentication.getName();
		return ResponseEntity.ok(beneficiaryService.updateBeneficiary(id, username, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteBeneficiary(@PathVariable Long id, Authentication authentication) {
		String username = authentication.getName();
		beneficiaryService.deleteBeneficiary(id, username);
		return ResponseEntity.ok("Beneficiary deleted successfully");
	}
}
package com.bankguard.banking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BeneficiaryRequest {

    @NotBlank(message = "Nickname is required")
    @Size(max = 100, message = "Nickname must not exceed 100 characters")
    private String nickname;

    @NotBlank(message = "Beneficiary account number is required")
    @Pattern(
        regexp = "^[0-9]{9,18}$",
        message = "Beneficiary account number must contain 9 to 18 digits"
    )
    private String beneficiaryAccountNumber;

    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    @NotBlank(message = "IFSC code is required")
    @Pattern(
        regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
        message = "Invalid IFSC code"
    )
    private String ifscCode;
}
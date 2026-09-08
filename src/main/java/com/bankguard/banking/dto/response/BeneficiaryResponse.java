package com.bankguard.banking.dto.response;

import com.bankguard.banking.entity.BeneficiaryStatus;

import lombok.Data;

@Data
public class BeneficiaryResponse {

    private Long id;
    private String nickname;
    private String beneficiaryAccountNumber;
    private String bankName;
    private String ifscCode;
    private BeneficiaryStatus status;
}
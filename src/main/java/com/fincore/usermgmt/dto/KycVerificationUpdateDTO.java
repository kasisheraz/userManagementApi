package com.fincore.usermgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycVerificationUpdateDTO {
    private String status;
    private String riskLevel;
    private String reviewResult;
    private Long reviewedById;
}

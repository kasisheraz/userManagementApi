package com.fincore.usermgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmlScreeningRequestDTO {
    private Long verificationId;
    private Long userId;
    private String screeningType;
    private Boolean matchFound;
    private Integer riskScore;
    private String matchDetails;
}

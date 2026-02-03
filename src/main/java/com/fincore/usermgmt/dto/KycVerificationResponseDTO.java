package com.fincore.usermgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycVerificationResponseDTO {
    private Long verificationId;
    private Long userId;
    private String verificationLevel;
    private String status;
    private String riskLevel;
    private String sumsubApplicantId;
    private String reviewResult;
    private Long reviewedById;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
}

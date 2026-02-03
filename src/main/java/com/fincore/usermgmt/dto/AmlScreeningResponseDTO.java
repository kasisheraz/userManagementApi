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
public class AmlScreeningResponseDTO {
    private Long screeningId;
    private Long verificationId;
    private Long userId;
    private String screeningType;
    private Boolean matchFound;
    private Integer riskScore;
    private String matchDetails;
    private LocalDateTime screenedAt;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
}

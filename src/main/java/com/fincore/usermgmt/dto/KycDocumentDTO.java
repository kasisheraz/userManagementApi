package com.fincore.usermgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for KYC Document response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycDocumentDTO {
    private Long id;
    private Integer verificationIdentifier;
    private Long organisationId;
    private String organisationName;
    private String documentType;
    private String sumsubDocumentIdentifier;
    private String fileName;
    private String fileUrl;
    private String status;
    private String reasonDescription;
    private Long verifiedById;
    private String verifiedByName;
    private LocalDateTime createdDatetime;
    private LocalDateTime lastModifiedDatetime;
}

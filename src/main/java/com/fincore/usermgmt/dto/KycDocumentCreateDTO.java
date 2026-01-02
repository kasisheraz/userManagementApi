package com.fincore.usermgmt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating/uploading a new KYC Document.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycDocumentCreateDTO {

    @NotNull(message = "Organisation ID is required")
    private Long organisationId;

    private Integer verificationIdentifier;

    @NotBlank(message = "Document type is required")
    private String documentType;

    @Size(max = 100, message = "Sumsub document identifier must not exceed 100 characters")
    private String sumsubDocumentIdentifier;

    @Size(max = 255, message = "File name must not exceed 255 characters")
    private String fileName;

    private String fileUrl;
}

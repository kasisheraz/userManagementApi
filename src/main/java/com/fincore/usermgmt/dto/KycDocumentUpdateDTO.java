package com.fincore.usermgmt.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating KYC Document verification status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycDocumentUpdateDTO {

    private String status;

    @Size(max = 1000, message = "Reason description must not exceed 1000 characters")
    private String reasonDescription;

    @Size(max = 100, message = "Sumsub document identifier must not exceed 100 characters")
    private String sumsubDocumentIdentifier;

    @Size(max = 255, message = "File name must not exceed 255 characters")
    private String fileName;

    private String fileUrl;
}

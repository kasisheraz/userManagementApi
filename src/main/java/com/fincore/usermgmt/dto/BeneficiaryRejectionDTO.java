package com.fincore.usermgmt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for rejecting or suspending a Beneficiary.
 * Contains only the rejection/suspension reason.
 * 
 * @author AI Assistant
 * @since 2.2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryRejectionDTO {

    @NotBlank(message = "Reason is required")
    @Size(min = 10, max = 255, message = "Reason must be between 10 and 255 characters")
    private String reason;
}

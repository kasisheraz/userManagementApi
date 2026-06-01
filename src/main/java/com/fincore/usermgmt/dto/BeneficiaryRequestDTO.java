package com.fincore.usermgmt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a Beneficiary.
 * Used for both POST and PUT requests.
 * 
 * @author AI Assistant
 * @since 2.2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryRequestDTO {

    @NotBlank(message = "Beneficiary name is required")
    @Size(max = 100, message = "Beneficiary name must not exceed 100 characters")
    private String beneficiaryName;

    @Size(max = 100, message = "Nick name must not exceed 100 characters")
    private String nickName;

    @Size(max = 100, message = "Business name must not exceed 100 characters")
    private String businessName;

    @NotBlank(message = "Country is required")
    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;

    @NotNull(message = "Registered address is required")
    private Long registeredAddressId;

    // Counter Over Counter Fields
    @NotNull(message = "Is Counter Over Counter flag is required")
    private Boolean isCounterOverCounter;

    @Size(max = 20, message = "Collector contact number must not exceed 20 characters")
    private String collectorContactNumber;

    /**
     * Custom validation: If isCounterOverCounter is true, collectorContactNumber is required.
     * This is validated in the service layer.
     */
    public boolean isValid() {
        if (Boolean.TRUE.equals(isCounterOverCounter)) {
            return collectorContactNumber != null && !collectorContactNumber.trim().isEmpty();
        }
        return true;
    }
}

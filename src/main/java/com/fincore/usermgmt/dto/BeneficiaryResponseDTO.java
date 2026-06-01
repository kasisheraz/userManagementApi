package com.fincore.usermgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Beneficiary response.
 * Used to return beneficiary data to clients.
 * 
 * @author AI Assistant
 * @since 2.2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryResponseDTO {
    
    private Long id;
    private Long ownerId;
    private String ownerName;
    
    // Core Fields
    private String beneficiaryName;
    private String nickName;
    private String businessName;
    private String country;
    
    // Address
    private AddressDTO registeredAddress;
    
    // Counter Over Counter Fields
    private Boolean isCounterOverCounter;
    private String collectorContactNumber;
    
    // Workflow Fields
    private String status;  // BeneficiaryStatus enum as string
    private String reasonDescription;
    
    // Audit Fields
    private LocalDateTime createdDatetime;
    private String createdByName;
    private LocalDateTime lastModifiedDatetime;
    private String lastModifiedByName;
    
    // Computed Fields (for UI convenience)
    private Boolean canBeEdited;      // True if status is PENDING
    private Boolean canBeSubmitted;   // True if status is PENDING and all required docs uploaded
    private Boolean isActive;         // True if status is ACTIVE
}

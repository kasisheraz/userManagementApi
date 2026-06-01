package com.fincore.usermgmt.entity;

/**
 * Enum representing beneficiary status in the workflow lifecycle.
 * 
 * <p>Status Flow:</p>
 * <pre>
 * PENDING → UNDER_REVIEW → ACTIVE
 *               ↓
 *          REJECTED (permanent)
 *               ↓
 *          SUSPENDED (temporary)
 * </pre>
 * 
 * @author AI Assistant
 * @since 2.2.0
 */
public enum BeneficiaryStatus {
    
    /**
     * Initial state after beneficiary creation.
     * Beneficiary can be edited and documents can be uploaded.
     */
    PENDING,
    
    /**
     * Beneficiary submitted for admin review.
     * Cannot be edited by owner. Admin can approve or reject.
     */
    UNDER_REVIEW,
    
    /**
     * Beneficiary approved by admin and fully active.
     * Can be used for transactions.
     */
    ACTIVE,
    
    /**
     * Beneficiary rejected by admin with feedback.
     * Cannot be resubmitted (permanent rejection).
     */
    REJECTED,
    
    /**
     * Beneficiary temporarily suspended by admin.
     * Can be reactivated later.
     */
    SUSPENDED
}

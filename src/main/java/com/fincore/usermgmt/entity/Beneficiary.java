package com.fincore.usermgmt.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Entity representing a Beneficiary (Payout Client).
 * 
 * <p>Beneficiaries are institutions on the receiving end of financial transactions.
 * Users can create up to 20 beneficiaries. Each beneficiary goes through an approval
 * workflow similar to Organization onboarding.</p>
 * 
 * <p>Workflow: PENDING → UNDER_REVIEW → ACTIVE/REJECTED</p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Counter Over Counter (C2C) collection support</li>
 *   <li>5 KYC document types (3 required, 1 conditional, 1 optional)</li>
 *   <li>Admin approval with document-level feedback</li>
 *   <li>Role-based access (users see only their beneficiaries)</li>
 * </ul>
 * 
 * @author AI Assistant
 * @since 2.2.0
 */
@Entity
@Table(name = "beneficiary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Beneficiary_Identifier")
    private Long id;

    @Column(name = "Beneficiary_Name", nullable = false, length = 100)
    private String beneficiaryName;

    @Column(name = "Nick_Name", length = 100)
    private String nickName;

    @Column(name = "Business_Name", length = 100)
    private String businessName;

    @Column(name = "Country", nullable = false, length = 50)
    private String country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "User_Identifier", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Registered_Address_Identifier")
    private Address registeredAddress;

    // Counter Over Counter Fields
    @Column(name = "Is_Counter_Over_Counter")
    private Boolean isCounterOverCounter;

    @Column(name = "Collector_Contact_Number", length = 20)
    private String collectorContactNumber;

    // Workflow Fields
    @Enumerated(EnumType.STRING)
    @Column(name = "Status_Description", length = 30)
    private BeneficiaryStatus status;

    @Column(name = "Reason_Description", length = 255)
    private String reasonDescription;

    // Audit Fields
    @Column(name = "Created_Datetime")
    private LocalDateTime createdDatetime;

    @Column(name = "Created_By")
    private Long createdBy;

    @Column(name = "Last_Modified_Datetime")
    private LocalDateTime lastModifiedDatetime;

    @Column(name = "Last_Modified_By")
    private Long lastModifiedBy;

    /**
     * JPA lifecycle callback executed before entity is persisted.
     * Sets default values for status and timestamps.
     */
    @PrePersist
    protected void onCreate() {
        createdDatetime = LocalDateTime.now();
        lastModifiedDatetime = LocalDateTime.now();
        if (status == null) {
            status = BeneficiaryStatus.PENDING;
        }
        if (isCounterOverCounter == null) {
            isCounterOverCounter = false;
        }
    }

    /**
     * JPA lifecycle callback executed before entity is updated.
     * Updates the last modified timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        lastModifiedDatetime = LocalDateTime.now();
    }

    /**
     * Business logic validation: Check if this is a Counter Over Counter beneficiary.
     * @return true if C2C collection is enabled
     */
    public boolean isC2C() {
        return Boolean.TRUE.equals(isCounterOverCounter);
    }

    /**
     * Check if beneficiary can be edited by owner.
     * @return true if status allows editing (PENDING only)
     */
    public boolean canBeEdited() {
        return status == BeneficiaryStatus.PENDING;
    }

    /**
     * Check if beneficiary can be submitted for review.
     * @return true if status is PENDING
     */
    public boolean canBeSubmitted() {
        return status == BeneficiaryStatus.PENDING;
    }

    /**
     * Check if beneficiary can be approved by admin.
     * @return true if status is UNDER_REVIEW
     */
    public boolean canBeApproved() {
        return status == BeneficiaryStatus.UNDER_REVIEW;
    }

    /**
     * Check if beneficiary can be rejected by admin.
     * @return true if status is UNDER_REVIEW
     */
    public boolean canBeRejected() {
        return status == BeneficiaryStatus.UNDER_REVIEW;
    }

    /**
     * Check if beneficiary is active and usable for transactions.
     * @return true if status is ACTIVE
     */
    public boolean isActive() {
        return status == BeneficiaryStatus.ACTIVE;
    }
}

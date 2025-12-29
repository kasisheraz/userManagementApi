package com.fincore.usermgmt.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "User_Identifier")
    private Long id;

    @Column(name = "Phone_Number", unique = true, length = 20)
    private String phoneNumber;

    @Column(name = "Email", length = 50)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Role_Identifier")
    private Role role;

    @Column(name = "First_Name", length = 100)
    private String firstName;
    
    @Column(name = "Middle_Name", length = 100)
    private String middleName;
    
    @Column(name = "Last_Name", length = 100)
    private String lastName;

    @Column(name = "Date_Of_Birth")
    private LocalDate dateOfBirth;

    @Column(name = "Residential_Address_Identifier")
    private Integer residentialAddressIdentifier;

    @Column(name = "Postal_Address_Identifier")
    private Integer postalAddressIdentifier;

    @Column(name = "Status_Description", length = 20)
    private String statusDescription;
    
    @Column(name = "Created_Datetime")
    private LocalDateTime createdDatetime;
    
    @Column(name = "Last_Modified_Datetime")
    private LocalDateTime lastModifiedDatetime;

    @PrePersist
    protected void onCreate() {
        createdDatetime = LocalDateTime.now();
        lastModifiedDatetime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDatetime = LocalDateTime.now();
    }
}

package com.fincore.usermgmt.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_tokens")
@Data
public class OtpToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Token_Id")
    private Long id;

    @Column(name = "Phone_Number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "Otp_Code", nullable = false, length = 6)
    private String otpCode;

    @Column(name = "Expires_At", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "Verified", nullable = false)
    private boolean verified = false;

    @Column(name = "Created_At", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

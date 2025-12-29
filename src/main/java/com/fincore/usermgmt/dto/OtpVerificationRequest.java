package com.fincore.usermgmt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpVerificationRequest {
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    
    @NotBlank(message = "OTP is required")
    private String otp;
}

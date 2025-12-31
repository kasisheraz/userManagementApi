package com.fincore.usermgmt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OtpResponse {
    private String message;
    private String phoneNumber;
    private Long expiresIn; // seconds
    
    // Only included in non-production environments for testing
    private String devOtp;
    
    public OtpResponse(String message, String phoneNumber, Long expiresIn) {
        this.message = message;
        this.phoneNumber = phoneNumber;
        this.expiresIn = expiresIn;
    }
}

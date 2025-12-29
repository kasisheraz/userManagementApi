package com.fincore.usermgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OtpResponse {
    private String message;
    private String phoneNumber;
    private Long expiresIn; // seconds
}

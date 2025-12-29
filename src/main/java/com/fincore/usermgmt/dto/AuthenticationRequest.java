package com.fincore.usermgmt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthenticationRequest {
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
}

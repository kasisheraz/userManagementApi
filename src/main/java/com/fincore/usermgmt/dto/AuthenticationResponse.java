package com.fincore.usermgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserDTO user;
    
    public AuthenticationResponse(String accessToken, Long expiresIn, UserDTO user) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }
}

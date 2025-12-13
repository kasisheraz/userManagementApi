package com.fincore.usermgmt.security;

import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    
    // This utility is no longer needed with simple API Key authentication
    // but kept for backward compatibility

    public String generateToken(String username) {
        return "api-key-" + username + "-" + System.currentTimeMillis();
    }

    public String extractUsername(String token) {
        // Not used in simple API key model
        return null;
    }

    public boolean isTokenValid(String token, String username) {
        // Not used in simple API key model
        return false;
    }
}

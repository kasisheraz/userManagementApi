package com.fincore.usermgmt.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 900000L);
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        String token = jwtUtil.generateToken("testuser");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);
        
        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    void isTokenValid_WithValidToken_ShouldReturnTrue() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);
        
        assertTrue(jwtUtil.isTokenValid(token, username));
    }

    @Test
    void isTokenValid_WithWrongUsername_ShouldReturnFalse() {
        String token = jwtUtil.generateToken("testuser");
        
        assertFalse(jwtUtil.isTokenValid(token, "wronguser"));
    }
}

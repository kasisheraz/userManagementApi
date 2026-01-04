package com.fincore.usermgmt.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private static final String TEST_SECRET = "aVerySecretKeyThatIsAtLeast256BitsLongForHS256AlgorithmToWorkProperly";
    private static final Long TEST_EXPIRATION = 3600000L; // 1 hour
    private static final String TEST_PHONE = "+1234567890";
    private static final Long TEST_USER_ID = 123L;
    private static final String TEST_ROLE = "ADMIN";

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", TEST_EXPIRATION);
    }

    // Token Generation Tests

    @Test
    void generateToken_withValidInputs_shouldReturnValidToken() {
        String token = tokenProvider.generateToken(TEST_PHONE, TEST_USER_ID, TEST_ROLE);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    void generateToken_withNullPhoneNumber_shouldGenerateToken() {
        String token = tokenProvider.generateToken(null, TEST_USER_ID, TEST_ROLE);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void generateToken_withNullUserId_shouldGenerateToken() {
        String token = tokenProvider.generateToken(TEST_PHONE, null, TEST_ROLE);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void generateToken_withNullRole_shouldGenerateToken() {
        String token = tokenProvider.generateToken(TEST_PHONE, TEST_USER_ID, null);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void generateToken_withAllNullValues_shouldGenerateToken() {
        String token = tokenProvider.generateToken(null, null, null);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void generateToken_withEmptyPhoneNumber_shouldGenerateToken() {
        String token = tokenProvider.generateToken("", TEST_USER_ID, TEST_ROLE);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void generateToken_withSpecialCharactersInRole_shouldGenerateToken() {
        String token = tokenProvider.generateToken(TEST_PHONE, TEST_USER_ID, "ROLE_ADMIN!@#$%");

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    // Token Validation Tests

    @Test
    void validateToken_withValidToken_shouldReturnTrue() {
        String token = tokenProvider.generateToken(TEST_PHONE, TEST_USER_ID, TEST_ROLE);

        boolean isValid = tokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_withInvalidToken_shouldReturnFalse() {
        String invalidToken = "invalid.token.here";

        boolean isValid = tokenProvider.validateToken(invalidToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_withMalformedToken_shouldReturnFalse() {
        String malformedToken = "notajwttoken";

        boolean isValid = tokenProvider.validateToken(malformedToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_withEmptyToken_shouldReturnFalse() {
        boolean isValid = tokenProvider.validateToken("");

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_withNullToken_shouldReturnFalse() {
        boolean isValid = tokenProvider.validateToken(null);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_withExpiredToken_shouldReturnFalse() {
        // Set very short expiration time
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", -1000L); // Already expired
        String expiredToken = tokenProvider.generateToken(TEST_PHONE, TEST_USER_ID, TEST_ROLE);
        
        // Reset to normal expiration for validation
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", TEST_EXPIRATION);

        boolean isValid = tokenProvider.validateToken(expiredToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_withTamperedSignature_shouldReturnFalse() {
        String token = tokenProvider.generateToken(TEST_PHONE, TEST_USER_ID, TEST_ROLE);
        // Tamper with the signature
        String[] parts = token.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".tampered";

        // validateToken catches the exception and returns false
        boolean isValid = tokenProvider.validateToken(tamperedToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_withDifferentSecret_shouldReturnFalse() {
        String token = tokenProvider.generateToken(TEST_PHONE, TEST_USER_ID, TEST_ROLE);
        
        // Create another provider with different secret
        JwtTokenProvider differentProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(differentProvider, "jwtSecret", 
            "differentSecretKeyThatIsAlsoAtLeast256BitsLongForHS256AlgorithmToWorkProperly");
        ReflectionTestUtils.setField(differentProvider, "jwtExpirationMs", TEST_EXPIRATION);

        // validateToken catches SignatureException and returns false
        boolean isValid = differentProvider.validateToken(token);

        assertThat(isValid).isFalse();
    }

    // Phone Number Extraction Tests

    @Test
    void getPhoneNumberFromToken_withValidToken_shouldReturnPhoneNumber() {
        String token = tokenProvider.generateToken(TEST_PHONE, TEST_USER_ID, TEST_ROLE);

        String phoneNumber = tokenProvider.getPhoneNumberFromToken(token);

        assertThat(phoneNumber).isEqualTo(TEST_PHONE);
    }

    @Test
    void getPhoneNumberFromToken_withNullPhoneInToken_shouldReturnNull() {
        String token = tokenProvider.generateToken(null, TEST_USER_ID, TEST_ROLE);

        String phoneNumber = tokenProvider.getPhoneNumberFromToken(token);

        assertThat(phoneNumber).isNull();
    }

    @Test
    void getPhoneNumberFromToken_withEmptyPhoneInToken_shouldReturnNull() {
        String token = tokenProvider.generateToken("", TEST_USER_ID, TEST_ROLE);

        String phoneNumber = tokenProvider.getPhoneNumberFromToken(token);

        // Empty string as subject becomes null
        assertThat(phoneNumber).isNull();
    }

    @Test
    void getPhoneNumberFromToken_withInvalidToken_shouldThrowException() {
        assertThatThrownBy(() -> tokenProvider.getPhoneNumberFromToken("invalid.token"))
                .isInstanceOf(Exception.class);
    }

    // User ID Extraction Tests

    @Test
    void getUserIdFromToken_withValidToken_shouldReturnUserId() {
        String token = tokenProvider.generateToken(TEST_PHONE, TEST_USER_ID, TEST_ROLE);

        Long userId = tokenProvider.getUserIdFromToken(token);

        assertThat(userId).isEqualTo(TEST_USER_ID);
    }

    @Test
    void getUserIdFromToken_withNullUserIdInToken_shouldReturnNull() {
        String token = tokenProvider.generateToken(TEST_PHONE, null, TEST_ROLE);

        Long userId = tokenProvider.getUserIdFromToken(token);

        assertThat(userId).isNull();
    }

    @Test
    void getUserIdFromToken_withZeroUserId_shouldReturnZero() {
        String token = tokenProvider.generateToken(TEST_PHONE, 0L, TEST_ROLE);

        Long userId = tokenProvider.getUserIdFromToken(token);

        assertThat(userId).isEqualTo(0L);
    }

    @Test
    void getUserIdFromToken_withNegativeUserId_shouldReturnNegativeValue() {
        String token = tokenProvider.generateToken(TEST_PHONE, -1L, TEST_ROLE);

        Long userId = tokenProvider.getUserIdFromToken(token);

        assertThat(userId).isEqualTo(-1L);
    }

    @Test
    void getUserIdFromToken_withLargeUserId_shouldReturnLargeValue() {
        Long largeUserId = Long.MAX_VALUE;
        String token = tokenProvider.generateToken(TEST_PHONE, largeUserId, TEST_ROLE);

        Long userId = tokenProvider.getUserIdFromToken(token);

        assertThat(userId).isEqualTo(largeUserId);
    }

    @Test
    void getUserIdFromToken_withInvalidToken_shouldThrowException() {
        assertThatThrownBy(() -> tokenProvider.getUserIdFromToken("invalid.token"))
                .isInstanceOf(Exception.class);
    }

    // Expiration Time Tests

    @Test
    void getExpirationTime_shouldReturnConfiguredValue() {
        Long expirationTime = tokenProvider.getExpirationTime();

        assertThat(expirationTime).isEqualTo(TEST_EXPIRATION);
    }

    @Test
    void getExpirationTime_withDifferentValue_shouldReturnNewValue() {
        Long newExpiration = 7200000L; // 2 hours
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", newExpiration);

        Long expirationTime = tokenProvider.getExpirationTime();

        assertThat(expirationTime).isEqualTo(newExpiration);
    }

    // Integration Tests - Full Token Lifecycle

    @Test
    void tokenLifecycle_generateValidateExtract_shouldWorkCorrectly() {
        // Generate
        String token = tokenProvider.generateToken(TEST_PHONE, TEST_USER_ID, TEST_ROLE);
        assertThat(token).isNotNull();

        // Validate
        boolean isValid = tokenProvider.validateToken(token);
        assertThat(isValid).isTrue();

        // Extract phone number
        String phoneNumber = tokenProvider.getPhoneNumberFromToken(token);
        assertThat(phoneNumber).isEqualTo(TEST_PHONE);

        // Extract user ID
        Long userId = tokenProvider.getUserIdFromToken(token);
        assertThat(userId).isEqualTo(TEST_USER_ID);
    }

    @Test
    void tokenLifecycle_withMinimalData_shouldWorkCorrectly() {
        String phone = "1";
        Long userId = 1L;
        String role = "U";

        String token = tokenProvider.generateToken(phone, userId, role);
        assertThat(tokenProvider.validateToken(token)).isTrue();
        assertThat(tokenProvider.getPhoneNumberFromToken(token)).isEqualTo(phone);
        assertThat(tokenProvider.getUserIdFromToken(token)).isEqualTo(userId);
    }

    @Test
    void tokenLifecycle_withMaximalData_shouldWorkCorrectly() {
        String longPhone = "+1234567890123456789";
        Long largeUserId = 999999999999L;
        String complexRole = "SUPER_ADMIN_ROLE_WITH_SPECIAL_PRIVILEGES";

        String token = tokenProvider.generateToken(longPhone, largeUserId, complexRole);
        assertThat(tokenProvider.validateToken(token)).isTrue();
        assertThat(tokenProvider.getPhoneNumberFromToken(token)).isEqualTo(longPhone);
        assertThat(tokenProvider.getUserIdFromToken(token)).isEqualTo(largeUserId);
    }

    // Token Claims Tests

    @Test
    void token_shouldContainAllClaims() {
        String token = tokenProvider.generateToken(TEST_PHONE, TEST_USER_ID, TEST_ROLE);

        // Parse token to verify claims
        var claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(TEST_PHONE);
        assertThat(claims.get("userId", Long.class)).isEqualTo(TEST_USER_ID);
        assertThat(claims.get("phoneNumber", String.class)).isEqualTo(TEST_PHONE);
        assertThat(claims.get("role", String.class)).isEqualTo(TEST_ROLE);
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
    }

    @Test
    void token_expirationDate_shouldBeCorrect() {
        String token = tokenProvider.generateToken(TEST_PHONE, TEST_USER_ID, TEST_ROLE);

        var claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();

        assertThat(issuedAt).isNotNull();
        assertThat(expiration).isNotNull();
        assertThat(expiration.getTime() - issuedAt.getTime()).isEqualTo(TEST_EXPIRATION);
    }

    // Edge Cases

    @Test
    void generateToken_multipleTimes_shouldGenerateDifferentTokens() {
        String token1 = tokenProvider.generateToken(TEST_PHONE, TEST_USER_ID, TEST_ROLE);
        
        // Small delay to ensure different issuedAt time (at least 1 second for different timestamp)
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String token2 = tokenProvider.generateToken(TEST_PHONE, TEST_USER_ID, TEST_ROLE);

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void validateToken_withWhitespaceToken_shouldReturnFalse() {
        boolean isValid = tokenProvider.validateToken("   ");

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_withTokenMissingParts_shouldReturnFalse() {
        String incompleteToken = "header.payload"; // Missing signature

        boolean isValid = tokenProvider.validateToken(incompleteToken);

        assertThat(isValid).isFalse();
    }
}

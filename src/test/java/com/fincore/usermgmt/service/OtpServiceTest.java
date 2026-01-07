package com.fincore.usermgmt.service;

import com.fincore.usermgmt.entity.OtpToken;
import com.fincore.usermgmt.repository.OtpTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpTokenRepository otpTokenRepository;

    @InjectMocks
    private OtpService otpService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(otpService, "otpExpirationSeconds", 300); // 5 minutes
        ReflectionTestUtils.setField(otpService, "otpLength", 6);
    }

    // ==================== Generate OTP Tests ====================

    @Test
    void generateOtp_ShouldReturnSixDigitOtp() {
        // Given
        String phoneNumber = "+44-7700-900123";
        when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(i -> i.getArgument(0));

        // When
        String otp = otpService.generateOtp(phoneNumber);

        // Then
        assertThat(otp).isNotNull();
        assertThat(otp).hasSize(6);
        assertThat(otp).matches("\\d{6}"); // Should be 6 digits
    }

    @Test
    void generateOtp_ShouldDeletePreviousUnverifiedTokens() {
        // Given
        String phoneNumber = "+44-7700-900123";
        when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(i -> i.getArgument(0));

        // When
        otpService.generateOtp(phoneNumber);

        // Then
        verify(otpTokenRepository).deleteUnverifiedTokensByPhoneNumber(phoneNumber);
    }

    @Test
    void generateOtp_ShouldSaveTokenWithCorrectExpiration() {
        // Given
        String phoneNumber = "+44-7700-900123";
        ArgumentCaptor<OtpToken> tokenCaptor = ArgumentCaptor.forClass(OtpToken.class);
        LocalDateTime beforeGeneration = LocalDateTime.now();

        when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(i -> i.getArgument(0));

        // When
        otpService.generateOtp(phoneNumber);

        // Then
        verify(otpTokenRepository).save(tokenCaptor.capture());
        OtpToken savedToken = tokenCaptor.getValue();

        assertThat(savedToken.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(savedToken.getOtpCode()).matches("\\d{6}");
        assertThat(savedToken.getExpiresAt())
                .isAfter(beforeGeneration.plusSeconds(295)) // Allow 5 seconds variance
                .isBefore(beforeGeneration.plusSeconds(305));
    }

    @Test
    void generateOtp_WithSamePhoneNumber_ShouldGenerateDifferentOtps() {
        // Given
        String phoneNumber = "+44-7700-900123";
        when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(i -> i.getArgument(0));

        // When
        String otp1 = otpService.generateOtp(phoneNumber);
        String otp2 = otpService.generateOtp(phoneNumber);

        // Then - OTPs should be different (very high probability)
        assertThat(otp1).isNotEqualTo(otp2);
        verify(otpTokenRepository, times(2)).save(any(OtpToken.class));
    }

    @Test
    void generateOtp_ShouldSetVerifiedToFalse() {
        // Given
        String phoneNumber = "+44-7700-900123";
        ArgumentCaptor<OtpToken> tokenCaptor = ArgumentCaptor.forClass(OtpToken.class);
        when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(i -> i.getArgument(0));

        // When
        otpService.generateOtp(phoneNumber);

        // Then
        verify(otpTokenRepository).save(tokenCaptor.capture());
        OtpToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.isVerified()).isFalse();
    }

    // ==================== Verify OTP Tests ====================

    @Test
    void verifyOtp_WithValidOtp_ShouldReturnTrue() {
        // Given
        String phoneNumber = "+44-7700-900123";
        String otpCode = "123456";

        OtpToken token = new OtpToken();
        token.setPhoneNumber(phoneNumber);
        token.setOtpCode(otpCode);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        token.setVerified(false);

        when(otpTokenRepository.findByPhoneNumberAndOtpCodeAndVerifiedFalse(phoneNumber, otpCode))
                .thenReturn(Optional.of(token));
        when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(i -> i.getArgument(0));

        // When
        boolean result = otpService.verifyOtp(phoneNumber, otpCode);

        // Then
        assertThat(result).isTrue();
        assertThat(token.isVerified()).isTrue();
        verify(otpTokenRepository).save(token);
    }

    @Test
    void verifyOtp_WithInvalidOtp_ShouldReturnFalse() {
        // Given
        String phoneNumber = "+44-7700-900123";
        String otpCode = "wrong";

        when(otpTokenRepository.findByPhoneNumberAndOtpCodeAndVerifiedFalse(phoneNumber, otpCode))
                .thenReturn(Optional.empty());

        // When
        boolean result = otpService.verifyOtp(phoneNumber, otpCode);

        // Then
        assertThat(result).isFalse();
        verify(otpTokenRepository, never()).save(any(OtpToken.class));
    }

    @Test
    void verifyOtp_WithExpiredOtp_ShouldReturnFalse() {
        // Given
        String phoneNumber = "+44-7700-900123";
        String otpCode = "123456";

        OtpToken token = new OtpToken();
        token.setPhoneNumber(phoneNumber);
        token.setOtpCode(otpCode);
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1)); // Expired 1 minute ago
        token.setVerified(false);

        when(otpTokenRepository.findByPhoneNumberAndOtpCodeAndVerifiedFalse(phoneNumber, otpCode))
                .thenReturn(Optional.of(token));

        // When
        boolean result = otpService.verifyOtp(phoneNumber, otpCode);

        // Then
        assertThat(result).isFalse();
        verify(otpTokenRepository, never()).save(any(OtpToken.class));
    }

    @Test
    void verifyOtp_WithAlreadyVerifiedOtp_ShouldReturnFalse() {
        // Given
        String phoneNumber = "+44-7700-900123";
        String otpCode = "123456";

        when(otpTokenRepository.findByPhoneNumberAndOtpCodeAndVerifiedFalse(phoneNumber, otpCode))
                .thenReturn(Optional.empty()); // Already verified tokens won't be found

        // When
        boolean result = otpService.verifyOtp(phoneNumber, otpCode);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void verifyOtp_ShouldOnlyVerifyUnverifiedTokens() {
        // Given
        String phoneNumber = "+44-7700-900123";
        String otpCode = "123456";

        // When
        otpService.verifyOtp(phoneNumber, otpCode);

        // Then
        verify(otpTokenRepository).findByPhoneNumberAndOtpCodeAndVerifiedFalse(phoneNumber, otpCode);
    }

    @Test
    void verifyOtp_WithOtpExpiringInOneSecond_ShouldStillWork() {
        // Given
        String phoneNumber = "+44-7700-900123";
        String otpCode = "123456";

        OtpToken token = new OtpToken();
        token.setPhoneNumber(phoneNumber);
        token.setOtpCode(otpCode);
        token.setExpiresAt(LocalDateTime.now().plusSeconds(1)); // Expires in 1 second
        token.setVerified(false);

        when(otpTokenRepository.findByPhoneNumberAndOtpCodeAndVerifiedFalse(phoneNumber, otpCode))
                .thenReturn(Optional.of(token));
        when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(i -> i.getArgument(0));

        // When
        boolean result = otpService.verifyOtp(phoneNumber, otpCode);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void verifyOtp_WithOtpJustExpired_ShouldReturnFalse() {
        // Given
        String phoneNumber = "+44-7700-900123";
        String otpCode = "123456";

        OtpToken token = new OtpToken();
        token.setPhoneNumber(phoneNumber);
        token.setOtpCode(otpCode);
        token.setExpiresAt(LocalDateTime.now().minusSeconds(1)); // Expired 1 second ago
        token.setVerified(false);

        when(otpTokenRepository.findByPhoneNumberAndOtpCodeAndVerifiedFalse(phoneNumber, otpCode))
                .thenReturn(Optional.of(token));

        // When
        boolean result = otpService.verifyOtp(phoneNumber, otpCode);

        // Then
        assertThat(result).isFalse();
    }

    // ==================== Cleanup Tests ====================

    @Test
    void cleanupExpiredTokens_ShouldCallRepositoryWithCurrentTime() {
        // When
        otpService.cleanupExpiredTokens();

        // Then
        verify(otpTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
    }

    @Test
    void cleanupExpiredTokens_ShouldDeleteExpiredTokensOnly() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        otpService.cleanupExpiredTokens();

        // Then
        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(otpTokenRepository).deleteExpiredTokens(timeCaptor.capture());

        // Verify time is approximately now (within 1 second)
        LocalDateTime capturedTime = timeCaptor.getValue();
        assertThat(capturedTime)
                .isAfter(now.minusSeconds(1))
                .isBefore(now.plusSeconds(1));
    }

    // ==================== Getter Tests ====================

    @Test
    void getOtpExpirationSeconds_ShouldReturnConfiguredValue() {
        // When
        Integer expiration = otpService.getOtpExpirationSeconds();

        // Then
        assertThat(expiration).isEqualTo(300);
    }

    @Test
    void getOtpExpirationSeconds_WithDifferentValue_ShouldReturnThatValue() {
        // Given
        ReflectionTestUtils.setField(otpService, "otpExpirationSeconds", 600);

        // When
        Integer expiration = otpService.getOtpExpirationSeconds();

        // Then
        assertThat(expiration).isEqualTo(600);
    }

    // ==================== Edge Case Tests ====================

    @Test
    void generateOtp_WithVeryLongPhoneNumber_ShouldWork() {
        // Given
        String phoneNumber = "+44-7700-900123-extension-12345";
        when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(i -> i.getArgument(0));

        // When
        String otp = otpService.generateOtp(phoneNumber);

        // Then
        assertThat(otp).isNotNull();
        assertThat(otp).hasSize(6);
        verify(otpTokenRepository).save(any(OtpToken.class));
    }

    @Test
    void verifyOtp_WithDifferentPhoneNumber_ShouldReturnFalse() {
        // Given
        String phoneNumber1 = "+44-7700-900123";
        String phoneNumber2 = "+44-7700-900456";
        String otpCode = "123456";

        when(otpTokenRepository.findByPhoneNumberAndOtpCodeAndVerifiedFalse(phoneNumber2, otpCode))
                .thenReturn(Optional.empty());

        // When
        boolean result = otpService.verifyOtp(phoneNumber2, otpCode);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void generateOtp_MultipleTimesForDifferentNumbers_ShouldDeleteCorrectTokens() {
        // Given
        String phoneNumber1 = "+44-7700-900123";
        String phoneNumber2 = "+44-7700-900456";
        when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(i -> i.getArgument(0));

        // When
        otpService.generateOtp(phoneNumber1);
        otpService.generateOtp(phoneNumber2);

        // Then
        verify(otpTokenRepository).deleteUnverifiedTokensByPhoneNumber(phoneNumber1);
        verify(otpTokenRepository).deleteUnverifiedTokensByPhoneNumber(phoneNumber2);
    }
}

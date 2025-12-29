package com.fincore.usermgmt.service;

import com.fincore.usermgmt.entity.OtpToken;
import com.fincore.usermgmt.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${otp.expiration:300}") // 5 minutes in seconds
    private Integer otpExpirationSeconds;

    @Value("${otp.length:6}")
    private Integer otpLength;

    @Transactional
    public String generateOtp(String phoneNumber) {
        // Delete any existing unverified OTPs for this phone number
        otpTokenRepository.deleteUnverifiedTokensByPhoneNumber(phoneNumber);

        // Generate new OTP
        String otpCode = generateRandomOtp();
        
        OtpToken otpToken = new OtpToken();
        otpToken.setPhoneNumber(phoneNumber);
        otpToken.setOtpCode(otpCode);
        otpToken.setExpiresAt(LocalDateTime.now().plusSeconds(otpExpirationSeconds));
        
        otpTokenRepository.save(otpToken);
        
        // In production, send SMS here
        log.info("Generated OTP for {}: {} (This would be sent via SMS in production)", phoneNumber, otpCode);
        
        return otpCode; // Return for development purposes only
    }

    @Transactional
    public boolean verifyOtp(String phoneNumber, String otpCode) {
        return otpTokenRepository.findByPhoneNumberAndOtpCodeAndVerifiedFalse(phoneNumber, otpCode)
                .filter(token -> token.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(token -> {
                    token.setVerified(true);
                    otpTokenRepository.save(token);
                    return true;
                })
                .orElse(false);
    }

    private String generateRandomOtp() {
        int otp = secureRandom.nextInt((int) Math.pow(10, otpLength));
        return String.format("%0" + otpLength + "d", otp);
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void cleanupExpiredTokens() {
        otpTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.debug("Cleaned up expired OTP tokens");
    }

    public Integer getOtpExpirationSeconds() {
        return otpExpirationSeconds;
    }
}

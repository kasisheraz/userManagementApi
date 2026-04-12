package com.fincore.usermgmt.service;

import com.fincore.usermgmt.entity.OtpToken;
import com.fincore.usermgmt.repository.OtpTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final Optional<SmsService> smsService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${otp.expiration:300}") // 5 minutes in seconds
    private Integer otpExpirationSeconds;

    @Value("${otp.length:6}")
    private Integer otpLength;

    @Value("${otp.retry.max:3}")
    private Integer maxRetries;

    @Value("${otp.retry.delay:100}")
    private Integer retryDelayMs;
    
    @Value("${sms.enabled:false}")
    private boolean smsEnabled;

    @Autowired
    public OtpService(OtpTokenRepository otpTokenRepository, Optional<SmsService> smsService) {
        this.otpTokenRepository = otpTokenRepository;
        this.smsService = smsService;
    }

    public String generateOtp(String phoneNumber) {
        // Retry logic to handle deadlocks
        int retries = 0;
        Exception lastException = null;
        
        while (retries < maxRetries) {
            try {
                return generateOtpWithTransaction(phoneNumber);
            } catch (DataAccessException e) {
                lastException = e;
                String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                
                // Check if it's a deadlock error
                if (errorMsg.contains("deadlock") || errorMsg.contains("lock wait timeout")) {
                    retries++;
                    log.warn("Deadlock detected on OTP generation for {}. Retry {}/{}", 
                            phoneNumber, retries, maxRetries);
                    
                    if (retries < maxRetries) {
                        try {
                            // Exponential backoff
                            Thread.sleep(retryDelayMs * (long) Math.pow(2, retries - 1));
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("OTP generation interrupted", ie);
                        }
                    }
                } else {
                    // Not a deadlock, rethrow immediately
                    throw e;
                }
            }
        }
        
        log.error("Failed to generate OTP after {} retries for {}", maxRetries, phoneNumber);
        throw new RuntimeException("Failed to generate OTP due to database contention. Please try again.", lastException);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    protected String generateOtpWithTransaction(String phoneNumber) {
        // Delete any existing unverified OTPs for this phone number
        // Using READ_COMMITTED isolation and REQUIRES_NEW propagation to minimize lock time
        try {
            otpTokenRepository.deleteUnverifiedTokensByPhoneNumber(phoneNumber);
            otpTokenRepository.flush(); // Force deletion to complete before insert
        } catch (Exception e) {
            log.debug("Cleanup of old OTPs failed (this is OK if none existed): {}", e.getMessage());
            // Continue anyway - we can insert even if delete fails
        }

        // Generate new OTP
        String otpCode = generateRandomOtp();
        
        OtpToken otpToken = new OtpToken();
        otpToken.setPhoneNumber(phoneNumber);
        otpToken.setOtpCode(otpCode);
        otpToken.setExpiresAt(LocalDateTime.now().plusSeconds(otpExpirationSeconds));
        
        otpTokenRepository.save(otpToken);
        
        // Send OTP via SMS if enabled
        if (smsEnabled && smsService.isPresent()) {
            try {
                smsService.get().sendOtp(phoneNumber, otpCode);
                log.info("OTP sent via SMS to {}", phoneNumber);
            } catch (Exception e) {
                log.error("Failed to send OTP via SMS to {}: {}", phoneNumber, e.getMessage());
                // Continue execution - OTP is saved in database for development fallback
            }
        } else {
            log.info("Generated OTP for {}: {} (SMS disabled - set SMS_ENABLED=true to enable SMS delivery)", 
                     phoneNumber, otpCode);
        }
        
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

package com.fincore.usermgmt.service;

import com.fincore.usermgmt.dto.AuthenticationResponse;
import com.fincore.usermgmt.dto.OtpResponse;
import com.fincore.usermgmt.dto.UserDTO;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.mapper.UserMapper;
import com.fincore.usermgmt.repository.UserRepository;
import com.fincore.usermgmt.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Transactional
    public OtpResponse initiateAuthentication(String phoneNumber) {
        // Verify user exists
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found with phone number: " + phoneNumber));

        // Check if user is active
        if (!"ACTIVE".equalsIgnoreCase(user.getStatusDescription())) {
            throw new RuntimeException("User account is not active");
        }

        // Generate and send OTP
        String otp = otpService.generateOtp(phoneNumber);
        
        log.info("OTP sent to phone number: {} (OTP: {} - for development only)", phoneNumber, otp);
        
        OtpResponse response = new OtpResponse(
                "OTP sent to " + maskPhoneNumber(phoneNumber) + ". Please verify to complete authentication.",
                phoneNumber,
                otpService.getOtpExpirationSeconds().longValue()
        );
        
        // Include OTP in response for non-production environments (npe, local-h2, test)
        if (isNonProductionEnvironment()) {
            response.setDevOtp(otp);
            log.info("DEV MODE: OTP included in response for testing");
        }
        
        return response;
    }
    
    private boolean isNonProductionEnvironment() {
        return activeProfile != null && 
               (activeProfile.contains("npe") || 
                activeProfile.contains("local") || 
                activeProfile.contains("test") ||
                activeProfile.contains("h2") ||
                activeProfile.contains("dev"));
    }

    @Transactional
    public AuthenticationResponse verifyOtpAndAuthenticate(String phoneNumber, String otpCode) {
        // Verify OTP
        if (!otpService.verifyOtp(phoneNumber, otpCode)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        // Get user details
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate JWT token
        String roleName = user.getRole() != null ? user.getRole().getName() : "USER";
        String token = jwtTokenProvider.generateToken(phoneNumber, user.getId(), roleName);

        // Convert to DTO
        UserDTO userDTO = userMapper.toUserDTO(user);

        log.info("User authenticated successfully: {}", phoneNumber);

        return new AuthenticationResponse(
                token,
                jwtTokenProvider.getExpirationTime() / 1000, // Convert to seconds
                userDTO
        );
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return phoneNumber;
        }
        int visibleDigits = 4;
        String masked = phoneNumber.substring(0, phoneNumber.length() - visibleDigits).replaceAll("\\d", "*");
        return masked + phoneNumber.substring(phoneNumber.length() - visibleDigits);
    }
}

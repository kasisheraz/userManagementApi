package com.fincore.usermgmt.repository;

import com.fincore.usermgmt.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findByPhoneNumberAndOtpCodeAndVerifiedFalse(String phoneNumber, String otpCode);
    
    @Modifying
    @Query(value = "DELETE FROM otp_tokens WHERE Expires_At < :now LIMIT 1000", nativeQuery = true)
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query(value = "DELETE FROM otp_tokens WHERE Phone_Number = :phoneNumber AND Verified = 0 LIMIT 100", nativeQuery = true)
    void deleteUnverifiedTokensByPhoneNumber(@Param("phoneNumber") String phoneNumber);
}

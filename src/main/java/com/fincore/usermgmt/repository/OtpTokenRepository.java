package com.fincore.usermgmt.repository;

import com.fincore.usermgmt.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findByPhoneNumberAndOtpCodeAndVerifiedFalse(String phoneNumber, String otpCode);
    
    @Modifying
    @Query("DELETE FROM OtpToken o WHERE o.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM OtpToken o WHERE o.phoneNumber = :phoneNumber AND o.verified = false")
    void deleteUnverifiedTokensByPhoneNumber(String phoneNumber);
}

package com.fincore.usermgmt.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Database migration to add indexes for OTP deadlock prevention
 * This runs once on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OtpIndexMigration {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void addOtpIndexes() {
        try {
            log.info("Checking and creating OTP table indexes for deadlock prevention...");
            
            // Create indexes if they don't exist (idempotent)
            // Note: MySQL 8+ supports "IF NOT EXISTS" in CREATE INDEX
            
            try {
                jdbcTemplate.execute(
                    "CREATE INDEX IF NOT EXISTS idx_otp_phone_verified ON otp_tokens(Phone_Number, Verified)"
                );
                log.info("Created/verified index: idx_otp_phone_verified");
            } catch (Exception e) {
                log.debug("Index idx_otp_phone_verified might already exist: {}", e.getMessage());
            }
            
            try {
                jdbcTemplate.execute(
                    "CREATE INDEX IF NOT EXISTS idx_otp_expires ON otp_tokens(Expires_At)"
                );
                log.info("Created/verified index: idx_otp_expires");
            } catch (Exception e) {
                log.debug("Index idx_otp_expires might already exist: {}", e.getMessage());
            }
            
            try {
                jdbcTemplate.execute(
                    "CREATE INDEX IF NOT EXISTS idx_otp_lookup ON otp_tokens(Phone_Number, Otp_Code, Verified)"
                );
                log.info("Created/verified index: idx_otp_lookup");
            } catch (Exception e) {
                log.debug("Index idx_otp_lookup might already exist: {}", e.getMessage());
            }
            
            // Analyze table for better query optimization
            try {
                jdbcTemplate.execute("ANALYZE TABLE otp_tokens");
                log.info("Analyzed otp_tokens table for optimization");
            } catch (Exception e) {
                log.warn("Could not analyze table (might be H2): {}", e.getMessage());
            }
            
            log.info("OTP table index migration completed successfully");
            
        } catch (Exception e) {
            log.error("Error during OTP index migration: {}", e.getMessage(), e);
            // Don't throw - let the application start even if indexes fail
        }
    }
}

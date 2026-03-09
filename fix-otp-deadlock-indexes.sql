-- Fix OTP Deadlock Issues
-- This script adds indexes and optimizes the otp_tokens table to prevent deadlocks

USE fincore_db;

-- Check current indexes
SHOW INDEX FROM otp_tokens;

-- Add indexes if they don't exist (MySQL will ignore if they already exist)
-- These are idempotent operations

-- Index for phone number lookups (used in DELETE operations)
CREATE INDEX IF NOT EXISTS idx_otp_phone_verified ON otp_tokens(Phone_Number, Verified);

-- Index for expiry cleanup (used in scheduled cleanup)
CREATE INDEX IF NOT EXISTS idx_otp_expires ON otp_tokens(Expires_At);

-- Composite index for the findByPhoneNumberAndOtpCodeAndVerifiedFalse query
CREATE INDEX IF NOT EXISTS idx_otp_lookup ON otp_tokens(Phone_Number, Otp_Code, Verified);

-- Show final index structure
SHOW INDEX FROM otp_tokens;

-- Analyze table for better query optimization
ANALYZE TABLE otp_tokens;

-- Show table status
SHOW TABLE STATUS LIKE 'otp_tokens';

SELECT 
    'Indexes created successfully for otp_tokens table to prevent deadlocks' AS Status,
    COUNT(*) AS Total_Records,
    SUM(CASE WHEN Verified = 1 THEN 1 ELSE 0 END) AS Verified_Count,
    SUM(CASE WHEN Verified = 0 THEN 1 ELSE 0 END) AS Unverified_Count,
    SUM(CASE WHEN Expires_At < NOW() THEN 1 ELSE 0 END) AS Expired_Count
FROM otp_tokens;

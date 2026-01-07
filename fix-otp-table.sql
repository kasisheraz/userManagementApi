-- Fix OTP_Tokens table to match entity definition
USE fincore_db;

-- Drop and recreate otp_tokens table with correct columns
DROP TABLE IF EXISTS otp_tokens;
DROP TABLE IF EXISTS Otp_Tokens;

CREATE TABLE otp_tokens (
    Token_Id BIGINT PRIMARY KEY AUTO_INCREMENT,
    Phone_Number VARCHAR(20) NOT NULL,
    Otp_Code VARCHAR(6) NOT NULL,
    Expires_At TIMESTAMP NOT NULL,
    Verified BOOLEAN NOT NULL DEFAULT FALSE,
    Created_At TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add index for phone number lookups
CREATE INDEX idx_phone_number ON otp_tokens(Phone_Number);
CREATE INDEX idx_verified ON otp_tokens(Verified);

SELECT 'OTP_Tokens table fixed!' AS Status;

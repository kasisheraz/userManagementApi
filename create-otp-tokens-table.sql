-- Create lowercase otp_tokens table for Hibernate
-- Copy structure from uppercase Otp_Tokens table but use lowercase name

CREATE TABLE IF NOT EXISTS otp_tokens (
    Token_Id BIGINT AUTO_INCREMENT PRIMARY KEY,
    Phone_Number VARCHAR(20) NOT NULL,
    Otp_Code VARCHAR(6) NOT NULL,
    Expires_At DATETIME NOT NULL,
    Verified BOOLEAN NOT NULL DEFAULT FALSE,
    Created_At DATETIME NOT NULL,
    INDEX idx_otp_phone_verified (Phone_Number, Verified),
    INDEX idx_otp_expires (Expires_At),
    INDEX idx_otp_lookup (Phone_Number, Otp_Code, Verified)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

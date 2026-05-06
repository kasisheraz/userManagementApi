-- Fix table name case mismatches for JPA entities
-- JPA entities use lowercase table names, but schema uses capital letters

-- Rename Otp_Tokens to otp_tokens
RENAME TABLE Otp_Tokens TO otp_tokens;

-- Verify tables
SHOW TABLES LIKE '%otp%';
SHOW TABLES LIKE '%users%';

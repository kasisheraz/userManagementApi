-- Expand Status_Description columns to accommodate all enum values
-- Bug fix: REQUIRES_RESUBMISSION (23 chars) doesn't fit in VARCHAR(20)

-- Update organisation table
ALTER TABLE organisation 
MODIFY COLUMN Status_Description VARCHAR(30);

-- Update kyc_document table
ALTER TABLE kyc_document
MODIFY COLUMN Status_Description VARCHAR(30);

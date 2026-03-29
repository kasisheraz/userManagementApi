-- Migration V4.0: Add Foreign Key Constraints to Users Table for Address References
-- This migration adds foreign key constraints to ensure referential integrity
-- between Users and Address tables for residential and postal addresses.

-- Add foreign key constraint for Residential_Address_Identifier
ALTER TABLE Users 
ADD CONSTRAINT fk_add1_id 
FOREIGN KEY (Residential_Address_Identifier) 
REFERENCES Address(Address_Identifier);

-- Add foreign key constraint for Postal_Address_Identifier
ALTER TABLE Users 
ADD CONSTRAINT fk_add2_id 
FOREIGN KEY (Postal_Address_Identifier) 
REFERENCES Address(Address_Identifier);

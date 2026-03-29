-- Migration V4.0: Foreign Key Constraints for Users Table Address References
-- NOTE: The FK constraints fk_add1_id and fk_add2_id for Residential_Address_Identifier
-- and Postal_Address_Identifier are already defined at the end of V1.0__Initial_Schema.sql.
-- This migration intentionally performs no additional schema changes to avoid duplicates.
SELECT 'FK constraints for Users address references are already defined in V1.0' AS migration_note;

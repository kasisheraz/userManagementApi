-- Drop Phase 2 tables (in correct order to handle foreign keys)
-- Run this BEFORE importing complete-entity-schema.sql

-- Drop dependent tables first (those with foreign keys)
DROP TABLE IF EXISTS customer_answers;
DROP TABLE IF EXISTS aml_screening_results;

-- Drop parent tables
DROP TABLE IF EXISTS questionnaire_questions;
DROP TABLE IF EXISTS customer_kyc_verification;

-- Now you can import complete-entity-schema.sql with the corrected schema

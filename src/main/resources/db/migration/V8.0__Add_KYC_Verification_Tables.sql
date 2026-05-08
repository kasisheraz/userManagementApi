-- ============================================
-- Flyway Migration V8.0
-- Add Default KYC Compliance Questions
-- ============================================
-- NOTE: All KYC tables already exist from V3.0 migration.
--       This migration only adds default compliance questions if not present.

-- Insert default questionnaire questions (only if they don't exist)
-- Using INSERT IGNORE to skip if questions already exist
INSERT IGNORE INTO questionnaire_questions (question_text, question_category, display_order, status) VALUES
('Are you a Politically Exposed Person (PEP) or have you been in the last 12 months?', 'PERSONAL', 1, 'ACTIVE'),
('Are any of your immediate family members or close associates Politically Exposed Persons?', 'PERSONAL', 2, 'ACTIVE'),
('Have you ever been subject to sanctions by any country or international organization?', 'PERSONAL', 3, 'ACTIVE'),
('Are you currently under investigation for any financial crimes?', 'PERSONAL', 4, 'ACTIVE'),
('Have you ever been convicted of a financial crime or money laundering offense?', 'PERSONAL', 5, 'ACTIVE'),
('Do you expect to conduct transactions exceeding £10,000 per month?', 'FINANCIAL', 6, 'ACTIVE'),
('Will you be conducting transactions on behalf of third parties?', 'FINANCIAL', 7, 'ACTIVE'),
('Are you acting on behalf of another individual or entity?', 'FINANCIAL', 8, 'ACTIVE'),
('Do you have tax residency in multiple countries?', 'FINANCIAL', 9, 'ACTIVE'),
('Are you a US citizen or US tax resident (for FATCA compliance)?', 'FINANCIAL', 10, 'ACTIVE');

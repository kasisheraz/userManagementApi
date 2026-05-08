-- ============================================
-- Flyway Migration V8.0
-- Customer KYC Verification Tables (Phase 3)
-- ============================================

-- Customer KYC verification table
CREATE TABLE IF NOT EXISTS customer_kyc_verification (
    verification_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_identifier INT NOT NULL,
    sumsub_applicant_id VARCHAR(100) UNIQUE,
    verification_level VARCHAR(50),
    status_description VARCHAR(50),
    reason_description VARCHAR(100),
    review_result JSON,
    risk_level ENUM('LOW', 'MEDIUM', 'HIGH'),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP NULL,
    approved_at TIMESTAMP NULL,
    rejected_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,
    created_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    last_modified_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_modified_by INT,
    INDEX idx_customer_id (customer_identifier),
    INDEX idx_status (status_description),
    INDEX idx_sumsub_applicant (sumsub_applicant_id),
    CONSTRAINT fk_customer_id_kyc FOREIGN KEY (customer_identifier) REFERENCES users(User_Identifier) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- AML screening results table
CREATE TABLE IF NOT EXISTS aml_screening_results (
    screening_id INT AUTO_INCREMENT PRIMARY KEY,
    verification_id INT NOT NULL,
    customer_identifier INT NOT NULL,
    screening_type ENUM('PEP', 'SANCTIONS', 'ADVERSE_MEDIA') NOT NULL,
    match_found BOOLEAN DEFAULT FALSE,
    match_details JSON,
    risk_score INT,
    screened_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    INDEX idx_verification_id (verification_id),
    INDEX idx_customer_id (customer_identifier),
    INDEX idx_screening_type (screening_type),
    INDEX idx_screened_at (screened_at),
    CONSTRAINT fk_verif_id FOREIGN KEY (verification_id) REFERENCES customer_kyc_verification(verification_id) ON DELETE CASCADE,
    CONSTRAINT fk_customer_id_aml FOREIGN KEY (customer_identifier) REFERENCES users(User_Identifier) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Questionnaire questions table
CREATE TABLE IF NOT EXISTS questionnaire_questions (
    question_id INT AUTO_INCREMENT PRIMARY KEY,
    question_text TEXT NOT NULL,
    question_category VARCHAR(50) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    status_description VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    last_modified_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_modified_by INT,
    INDEX idx_status (status_description),
    INDEX idx_category (question_category),
    INDEX idx_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Customer answers table
CREATE TABLE IF NOT EXISTS customer_answers (
    answer_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_identifier INT NOT NULL,
    question_id INT NOT NULL,
    answer ENUM('YES', 'NO') NOT NULL,
    answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by_user_identifier INT,
    last_modified_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_modified_by_user_identifier INT,
    INDEX idx_customer_id (customer_identifier),
    INDEX idx_question_id (question_id),
    INDEX idx_answered_at (answered_at),
    UNIQUE KEY unique_customer_question (customer_identifier, question_id),
    CONSTRAINT fk_customer_id_ans FOREIGN KEY (customer_identifier) REFERENCES users(User_Identifier) ON DELETE CASCADE,
    CONSTRAINT fk_qst_id FOREIGN KEY (question_id) REFERENCES questionnaire_questions(question_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default questionnaire questions
INSERT INTO questionnaire_questions (question_text, question_category, display_order, status_description) VALUES
('Are you a Politically Exposed Person (PEP) or have you been in the last 12 months?', 'PEP_SCREENING', 1, 'ACTIVE'),
('Are any of your immediate family members or close associates Politically Exposed Persons?', 'PEP_SCREENING', 2, 'ACTIVE'),
('Have you ever been subject to sanctions by any country or international organization?', 'SANCTIONS', 3, 'ACTIVE'),
('Are you currently under investigation for any financial crimes?', 'COMPLIANCE', 4, 'ACTIVE'),
('Have you ever been convicted of a financial crime or money laundering offense?', 'COMPLIANCE', 5, 'ACTIVE'),
('Do you expect to conduct transactions exceeding £10,000 per month?', 'TRANSACTION_LIMITS', 6, 'ACTIVE'),
('Will you be conducting transactions on behalf of third parties?', 'THIRD_PARTY', 7, 'ACTIVE'),
('Are you acting on behalf of another individual or entity?', 'BENEFICIAL_OWNER', 8, 'ACTIVE'),
('Do you have tax residency in multiple countries?', 'TAX_RESIDENCY', 9, 'ACTIVE'),
('Are you a US citizen or US tax resident (for FATCA compliance)?', 'TAX_RESIDENCY', 10, 'ACTIVE');

-- Add comments for documentation
ALTER TABLE customer_kyc_verification COMMENT = 'Stores customer KYC verification records with SumSub integration';
ALTER TABLE aml_screening_results COMMENT = 'Stores AML screening results (PEP, Sanctions, Adverse Media)';
ALTER TABLE questionnaire_questions COMMENT = 'Reusable compliance questionnaire questions';
ALTER TABLE customer_answers COMMENT = 'Customer responses to compliance questionnaire';

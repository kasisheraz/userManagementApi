-- V3.0__Create_KYC_AML_Verification_Tables.sql
-- Creates tables for KYC and AML verification workflow
-- Uses IF NOT EXISTS to be safe on databases that already have these tables

CREATE TABLE IF NOT EXISTS customer_kyc_verification (
    verification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    verification_level VARCHAR(50) NOT NULL COMMENT 'BASIC, FULL, AML',
    status VARCHAR(50) NOT NULL COMMENT 'PENDING, APPROVED, REJECTED, EXPIRED',
    reason VARCHAR(100),
    risk_level VARCHAR(50) COMMENT 'LOW, MEDIUM, HIGH',
    sumsub_applicant_id VARCHAR(255) UNIQUE,
    submitted_at DATETIME,
    reviewed_at DATETIME,
    approved_at DATETIME,
    rejected_at DATETIME,
    expires_at DATETIME,
    review_result JSON COMMENT 'Stores detailed review information',
    reviewed_by BIGINT,
    created_by BIGINT,
    last_modified_by BIGINT,
    created_datetime DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_datetime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_kyc_user FOREIGN KEY (user_id) REFERENCES Users(User_Identifier) ON DELETE CASCADE,
    CONSTRAINT fk_kyc_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES Users(User_Identifier),
    CONSTRAINT fk_kyc_created_by FOREIGN KEY (created_by) REFERENCES Users(User_Identifier),
    CONSTRAINT fk_kyc_modified_by FOREIGN KEY (last_modified_by) REFERENCES Users(User_Identifier),

    INDEX idx_kyc_user_id (user_id),
    INDEX idx_kyc_status (status),
    INDEX idx_kyc_verification_level (verification_level),
    INDEX idx_kyc_sumsub_applicant_id (sumsub_applicant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Stores KYC verification records';

CREATE TABLE IF NOT EXISTS aml_screening_results (
    screening_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    verification_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    screening_type VARCHAR(50) NOT NULL COMMENT 'SANCTIONS, PEP, ADVERSE_MEDIA',
    match_found BOOLEAN DEFAULT FALSE,
    risk_score INT CHECK (risk_score >= 0 AND risk_score <= 100),
    match_details JSON COMMENT 'Stores match information and confidence scores',
    screened_at DATETIME,
    created_by BIGINT,
    last_modified_by BIGINT,
    created_datetime DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_datetime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_aml_verification FOREIGN KEY (verification_id) REFERENCES customer_kyc_verification(verification_id) ON DELETE CASCADE,
    CONSTRAINT fk_aml_user FOREIGN KEY (user_id) REFERENCES Users(User_Identifier) ON DELETE CASCADE,
    CONSTRAINT fk_aml_created_by FOREIGN KEY (created_by) REFERENCES Users(User_Identifier),
    CONSTRAINT fk_aml_modified_by FOREIGN KEY (last_modified_by) REFERENCES Users(User_Identifier),

    INDEX idx_aml_verification_id (verification_id),
    INDEX idx_aml_user_id (user_id),
    INDEX idx_aml_screening_type (screening_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Stores AML screening results';

CREATE TABLE IF NOT EXISTS questionnaire_questions (
    question_id INT AUTO_INCREMENT PRIMARY KEY,
    question_text TEXT,
    question_category VARCHAR(50) COMMENT 'PERSONAL, EMPLOYMENT, FINANCIAL',
    status VARCHAR(50) DEFAULT 'ACTIVE' COMMENT 'ACTIVE, INACTIVE, ARCHIVED',
    display_order INT,
    created_by BIGINT,
    last_modified_by BIGINT,
    created_datetime DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_datetime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_question_created_by FOREIGN KEY (created_by) REFERENCES Users(User_Identifier),
    CONSTRAINT fk_question_modified_by FOREIGN KEY (last_modified_by) REFERENCES Users(User_Identifier),

    INDEX idx_qq_status (status),
    INDEX idx_qq_category (question_category),
    INDEX idx_qq_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Stores questionnaire questions';

CREATE TABLE IF NOT EXISTS customer_answers (
    answer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question_id INT NOT NULL,
    answer VARCHAR(500),
    answered_at DATETIME,
    created_by BIGINT,
    last_modified_by BIGINT,
    created_datetime DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified_datetime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_answer_user FOREIGN KEY (user_id) REFERENCES Users(User_Identifier) ON DELETE CASCADE,
    CONSTRAINT fk_answer_question FOREIGN KEY (question_id) REFERENCES questionnaire_questions(question_id) ON DELETE CASCADE,
    CONSTRAINT fk_answer_created_by FOREIGN KEY (created_by) REFERENCES Users(User_Identifier),
    CONSTRAINT fk_answer_modified_by FOREIGN KEY (last_modified_by) REFERENCES Users(User_Identifier),

    UNIQUE KEY uc_user_question (user_id, question_id),
    INDEX idx_ca_user_id (user_id),
    INDEX idx_ca_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Stores customer answers to questionnaire questions';

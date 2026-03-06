-- ============================================
-- TEST DATA INSERTION SCRIPT
-- Comprehensive test data for all tables
-- For UI testing and demonstration
-- ============================================

USE fincore_db;

-- ============================================
-- 1. PERMISSIONS
-- ============================================
INSERT INTO permissions (Permission_Name, Description, Resource, Action) VALUES
('USER_READ', 'View user information', 'USER', 'READ'),
('USER_CREATE', 'Create new users', 'USER', 'CREATE'),
('USER_UPDATE', 'Update user information', 'USER', 'UPDATE'),
('USER_DELETE', 'Delete users', 'USER', 'DELETE'),
('ORG_READ', 'View organization information', 'ORGANISATION', 'READ'),
('ORG_CREATE', 'Create new organizations', 'ORGANISATION', 'CREATE'),
('ORG_UPDATE', 'Update organization information', 'ORGANISATION', 'UPDATE'),
('ORG_DELETE', 'Delete organizations', 'ORGANISATION', 'DELETE'),
('KYC_READ', 'View KYC documents', 'KYC', 'READ'),
('KYC_VERIFY', 'Verify KYC documents', 'KYC', 'VERIFY'),
('KYC_APPROVE', 'Approve KYC submissions', 'KYC', 'APPROVE'),
('AML_READ', 'View AML screening results', 'AML', 'READ'),
('AML_SCREEN', 'Perform AML screening', 'AML', 'SCREEN'),
('QUESTION_READ', 'View questionnaire questions', 'QUESTIONNAIRE', 'READ'),
('QUESTION_MANAGE', 'Manage questionnaire questions', 'QUESTIONNAIRE', 'MANAGE'),
('ANSWER_READ', 'View customer answers', 'ANSWER', 'READ'),
('ANSWER_CREATE', 'Submit answers', 'ANSWER', 'CREATE');

-- ============================================
-- 2. ROLES
-- ============================================
INSERT INTO roles (Role_Name, Role_Description) VALUES
('ADMIN', 'System administrator with full access'),
('COMPLIANCE_OFFICER', 'Compliance and KYC verification officer'),
('OPERATIONAL_STAFF', 'Operational staff with limited access'),
('USER', 'Regular user with basic access');

-- ============================================
-- 3. ROLE_PERMISSIONS (Assign permissions to roles)
-- ============================================
-- ADMIN gets all permissions
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier)
SELECT 1, Permission_Identifier FROM permissions;

-- COMPLIANCE_OFFICER gets KYC, AML, User read, Org read
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier)
SELECT 2, Permission_Identifier FROM permissions 
WHERE Permission_Name IN ('USER_READ', 'ORG_READ', 'KYC_READ', 'KYC_VERIFY', 'KYC_APPROVE', 'AML_READ', 'AML_SCREEN', 'QUESTION_READ', 'ANSWER_READ');

-- OPERATIONAL_STAFF gets read access and some create/update
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier)
SELECT 3, Permission_Identifier FROM permissions 
WHERE Permission_Name IN ('USER_READ', 'USER_UPDATE', 'ORG_READ', 'ORG_UPDATE', 'KYC_READ', 'AML_READ', 'QUESTION_READ', 'ANSWER_READ');

-- USER gets basic read and create access
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier)
SELECT 4, Permission_Identifier FROM permissions 
WHERE Permission_Name IN ('USER_READ', 'ORG_READ', 'QUESTION_READ', 'ANSWER_CREATE');

-- ============================================
-- 4. USERS
-- ============================================
INSERT INTO users (Phone_Number, Email, Role_Identifier, First_Name, Middle_Name, Last_Name, Date_Of_Birth, Status_Description) VALUES
-- Existing admin user
('+1234567890', 'admin@fincore.com', 1, 'System', NULL, 'Administrator', '1990-01-01', 'ACTIVE'),

-- Additional users with different roles
('+447911123456', 'sarah.williams@fintech.com', 2, 'Sarah', 'Jane', 'Williams', '1985-03-15', 'ACTIVE'),
('+447911123457', 'john.smith@fintech.com', 2, 'John', 'David', 'Smith', '1988-07-22', 'ACTIVE'),
('+447911123458', 'emma.brown@fintech.com', 3, 'Emma', 'Louise', 'Brown', '1992-11-08', 'ACTIVE'),
('+447911123459', 'michael.jones@fintech.com', 3, 'Michael', 'Robert', 'Jones', '1990-05-14', 'ACTIVE'),
('+447911123460', 'olivia.taylor@business.com', 4, 'Olivia', 'Grace', 'Taylor', '1995-09-20', 'ACTIVE'),
('+447911123461', 'james.wilson@business.com', 4, 'James', 'Alexander', 'Wilson', '1987-12-03', 'ACTIVE'),
('+447911123462', 'sophia.davies@startup.com', 4, 'Sophia', 'Emily', 'Davies', '1993-04-18', 'ACTIVE'),
('+447911123463', 'william.evans@corp.com', 4, 'William', 'Thomas', 'Evans', '1991-08-25', 'ACTIVE'),
('+447911123464', 'isabella.moore@enterprise.com', 4, 'Isabella', 'Rose', 'Moore', '1989-02-11', 'ACTIVE'),
('+447911123465', 'oliver.martin@company.com', 4, 'Oliver', 'James', 'Martin', '1994-06-30', 'INACTIVE'),
('+447911123466', 'ava.clark@business.co.uk', 4, 'Ava', 'Charlotte', 'Clark', '1996-10-05', 'PENDING');

-- ============================================
-- 5. ADDRESSES
-- ============================================
INSERT INTO address (Type_Code, Address_Line1, Address_Line2, Postal_Code, State_Code, City, Country, Status_Description, Created_By) VALUES
-- Type 1 = Residential, 2 = Business, 3 = Registered, 4 = Correspondence, 5 = Postal

-- Business addresses for organizations
(2, '123 Fintech Square', 'Floor 5', 'EC1A 1BB', 'LDN', 'London', 'United Kingdom', 'ACTIVE', 1),
(2, '456 Innovation Way', 'Suite 200', 'M1 1AE', 'MCR', 'Manchester', 'United Kingdom', 'ACTIVE', 1),
(2, '789 Tech Boulevard', NULL, 'B1 1TT', 'BHM', 'Birmingham', 'United Kingdom', 'ACTIVE', 1),
(2, '321 Digital Street', 'Building A', 'EH1 1YZ', 'EDI', 'Edinburgh', 'United Kingdom', 'ACTIVE', 1),
(2, '654 Commerce Road', NULL, 'LS1 1BA', 'LDS', 'Leeds', 'United Kingdom', 'ACTIVE', 1),

-- Registered addresses (same as business for some)
(3, '123 Fintech Square', 'Floor 5', 'EC1A 1BB', 'LDN', 'London', 'United Kingdom', 'ACTIVE', 1),
(3, '456 Innovation Way', 'Suite 200', 'M1 1AE', 'MCR', 'Manchester', 'United Kingdom', 'ACTIVE', 1),
(3, '789 Tech Boulevard', NULL, 'B1 1TT', 'BHM', 'Birmingham', 'United Kingdom', 'ACTIVE', 1),
(3, '50 Registrars Lane', NULL, 'CF10 1AA', 'CDF', 'Cardiff', 'United Kingdom', 'ACTIVE', 1),
(3, '99 Corporate Avenue', 'Floor 10', 'G1 1AA', 'GLA', 'Glasgow', 'United Kingdom', 'ACTIVE', 1),

-- Correspondence addresses
(4, '100 Post Office Street', NULL, 'SW1A 1AA', 'LDN', 'London', 'United Kingdom', 'ACTIVE', 1),
(4, '200 Mail Centre Road', NULL, 'M2 2BB', 'MCR', 'Manchester', 'United Kingdom', 'ACTIVE', 1),

-- Residential addresses for users
(1, '15 Oak Avenue', 'Flat 3B', 'W1A 1AA', 'LDN', 'London', 'United Kingdom', 'ACTIVE', 1),
(1, '28 Maple Drive', NULL, 'M3 3CD', 'MCR', 'Manchester', 'United Kingdom', 'ACTIVE', 1),
(1, '42 Birch Lane', 'Apartment 7', 'B2 2EF', 'BHM', 'Birmingham', 'United Kingdom', 'ACTIVE', 1);

-- ============================================
-- 6. ORGANISATIONS
-- ============================================
INSERT INTO organisation (
    User_Identifier, 
    Registration_Number, 
    SIC_Code,
    Legal_Name, 
    Business_Name, 
    Organisation_Type_Description,
    Business_Description,
    Incorporation_Date,
    Country_Of_Incorporation,
    HMRC_MLR_Number,
    HMRC_Expiry_Date,
    FCA_Number,
    ICO_Number,
    Number_Of_Branches,
    Number_Of_Agents,
    Company_Number,
    Website_Address,
    Monthly_Turnover_Range,
    Number_Of_Incoming_Transactions,
    Number_Of_Outgoing_Transactions,
    Primary_Remittance_Destination_Country,
    Registered_Address_Identifier,
    Business_Address_Identifier,
    Correspondence_Address_Identifier,
    Status_Description,
    Created_By
) VALUES
-- Organization 1: Large Money Transfer Company
(1, 'GB123456789', '64110', 'FinTech Solutions Limited', 'FinTech Solutions', 'LTD',
 'International money transfer and payment services',
 '2015-03-15', 'United Kingdom',
 'XFML12345678', '2026-12-31',
 'FCA789012', 'ICO456789',
 '5', '10',
 '09876543', 'www.fintechsolutions.com',
 '£500,000 - £1,000,000', '500', '450',
 'United States',
 6, 1, 11,
 'ACTIVE', 1),

-- Organization 2: Small Startup
(6, 'GB987654321', '62020', 'Digital Payments UK Ltd', 'DigiPay UK', 'LTD',
 'Digital wallet and payment processing',
 '2022-08-20', 'United Kingdom',
 'XMLR87654321', '2027-08-31',
 'FCA654321', 'ICO987654',
 '2', '5',
 '12345678', 'www.digipayuk.com',
 '£100,000 - £250,000', '200', '180',
 'Germany',
 7, 2, 12,
 'ACTIVE', 1),

-- Organization 3: Medium Enterprise
(7, 'GB456789123', '64191', 'GlobalTransfer Corp', 'GlobalTransfer', 'PLC',
 'Cross-border payment and remittance services',
 '2018-11-05', 'United Kingdom',
 'XMML45678912', '2026-11-30',
 'FCA123789', 'ICO321654',
 '8', '20',
 '56789012', 'www.globaltransfer.co.uk',
 '£1,000,000 - £5,000,000', '1000', '950',
 'India',
 8, 3, 11,
 'ACTIVE', 1),

-- Organization 4: Charity
(8, 'GB321654987', '88990', 'Financial Inclusion Foundation', 'FI Foundation', 'CHARITY',
 'Non-profit organization providing financial literacy and services',
 '2020-01-10', 'United Kingdom',
 NULL, NULL,
 NULL, 'ICO741852',
 '3', '0',
 'CH789456', 'www.fifoundation.org',
 'Under £100,000', '50', '30',
 'United Kingdom',
 9, 4, 11,
 'ACTIVE', 1),

-- Organization 5: Sole Trader
(9, NULL, '66190', 'Smith Financial Consulting', 'Smith FC', 'SOLE_TRADER',
 'Financial consulting and advisory services',
 '2023-04-01', 'United Kingdom',
 NULL, NULL,
 NULL, NULL,
 '1', '0',
 NULL, 'www.smithfc.com',
 'Under £100,000', '20', '15',
 'United Kingdom',
 10, 5, 11,
 'ACTIVE', 1),

-- Organization 6: Partnership
(10, 'GB789123456', '64999', 'Brown & Associates LLP', 'Brown Associates', 'LLP',
 'Payment processing and merchant services',
 '2019-06-15', 'United Kingdom',
 'XMLL78912345', '2027-06-30',
 'FCA456123', 'ICO159753',
 '4', '8',
 'OC234567', 'www.brownassociates.co.uk',
 '£250,000 - £500,000', '300', '280',
 'France',
 6, 1, 12,
 'ACTIVE', 1),

-- Organization 7: Pending Approval
(11, 'GB159753486', '64920', 'NewPay Technologies Ltd', 'NewPay Tech', 'LTD',
 'Mobile payment solutions and API services',
 '2025-12-01', 'United Kingdom',
 'XFML15975348', '2028-12-31',
 NULL, NULL,
 '1', '2',
 '13579246', 'www.newpaytech.com',
 'Under £100,000', '50', '40',
 'Netherlands',
 7, 2, 11,
 'PENDING', 1),

-- Organization 8: Suspended
(12, 'GB753951456', '64110', 'Legacy Transfers Ltd', 'Legacy Transfers', 'LTD',
 'Traditional money transfer services',
 '2010-03-20', 'United Kingdom',
 'XMLR75395145', '2024-12-31',
 'FCA852741', 'ICO963852',
 '10', '25',
 '98765432', 'www.legacytransfers.co.uk',
 '£1,000,000 - £5,000,000', '800', '750',
 'Pakistan',
 8, 3, 12,
 'SUSPENDED', 1);

-- ============================================
-- 7. KYC DOCUMENTS
-- ============================================
INSERT INTO kyc_documents (
    Reference_Identifier,
    Document_Type_Description,
    Sumsub_Document_Identifier,
    File_Name,
    File_URL,
    Status_Description,
    Document_Verified_By,
    Created_By
) VALUES
-- Documents for Organization 1 (FinTech Solutions)
(1, 'CERTIFICATE_OF_INCORPORATION', 'SUMSUB_DOC_001', 'cert_of_inc_fintech.pdf', 
 'https://storage.googleapis.com/fincore-docs/cert_of_inc_fintech.pdf', 'VERIFIED', 2, 1),
(1, 'PROOF_OF_ADDRESS', 'SUMSUB_DOC_002', 'proof_address_fintech.pdf',
 'https://storage.googleapis.com/fincore-docs/proof_address_fintech.pdf', 'VERIFIED', 2, 1),
(1, 'DIRECTORS_ID', 'SUMSUB_DOC_003', 'director_id_fintech.pdf',
 'https://storage.googleapis.com/fincore-docs/director_id_fintech.pdf', 'VERIFIED', 3, 1),
(1, 'FCA_LICENSE', 'SUMSUB_DOC_004', 'fca_license_fintech.pdf',
 'https://storage.googleapis.com/fincore-docs/fca_license_fintech.pdf', 'VERIFIED', 2, 1),

-- Documents for Organization 2 (DigiPay UK)
(2, 'CERTIFICATE_OF_INCORPORATION', 'SUMSUB_DOC_005', 'cert_of_inc_digipay.pdf',
 'https://storage.googleapis.com/fincore-docs/cert_of_inc_digipay.pdf', 'VERIFIED', 2, 6),
(2, 'PROOF_OF_ADDRESS', 'SUMSUB_DOC_006', 'proof_address_digipay.pdf',
 'https://storage.googleapis.com/fincore-docs/proof_address_digipay.pdf', 'VERIFIED', 3, 6),
(2, 'DIRECTORS_ID', 'SUMSUB_DOC_007', 'director_id_digipay.pdf',
 'https://storage.googleapis.com/fincore-docs/director_id_digipay.pdf', 'PENDING', NULL, 6),

-- Documents for Organization 3 (GlobalTransfer)
(3, 'CERTIFICATE_OF_INCORPORATION', 'SUMSUB_DOC_008', 'cert_of_inc_global.pdf',
 'https://storage.googleapis.com/fincore-docs/cert_of_inc_global.pdf', 'VERIFIED', 2, 7),
(3, 'PROOF_OF_ADDRESS', 'SUMSUB_DOC_009', 'proof_address_global.pdf',
 'https://storage.googleapis.com/fincore-docs/proof_address_global.pdf', 'VERIFIED', 2, 7),
(3, 'FINANCIAL_STATEMENTS', 'SUMSUB_DOC_010', 'financials_global.pdf',
 'https://storage.googleapis.com/fincore-docs/financials_global.pdf', 'VERIFIED', 3, 7),

-- Documents for Organization 4 (Charity)
(4, 'CHARITY_REGISTRATION', 'SUMSUB_DOC_011', 'charity_reg.pdf',
 'https://storage.googleapis.com/fincore-docs/charity_reg.pdf', 'VERIFIED', 2, 8),
(4, 'PROOF_OF_ADDRESS', 'SUMSUB_DOC_012', 'proof_address_charity.pdf',
 'https://storage.googleapis.com/fincore-docs/proof_address_charity.pdf', 'VERIFIED', 3, 8),

-- Documents for Organization 7 (Pending - NewPay)
(7, 'CERTIFICATE_OF_INCORPORATION', 'SUMSUB_DOC_013', 'cert_of_inc_newpay.pdf',
 'https://storage.googleapis.com/fincore-docs/cert_of_inc_newpay.pdf', 'PENDING', NULL, 11),
(7, 'PROOF_OF_ADDRESS', 'SUMSUB_DOC_014', 'proof_address_newpay.pdf',
 'https://storage.googleapis.com/fincore-docs/proof_address_newpay.pdf', 'PENDING', NULL, 11),

-- Documents for Organization 8 (Suspended - Legacy)
(8, 'CERTIFICATE_OF_INCORPORATION', 'SUMSUB_DOC_015', 'cert_of_inc_legacy.pdf',
 'https://storage.googleapis.com/fincore-docs/cert_of_inc_legacy.pdf', 'REJECTED', 2, 12),
(8, 'FCA_LICENSE', 'SUMSUB_DOC_016', 'fca_license_legacy.pdf',
 'https://storage.googleapis.com/fincore-docs/fca_license_legacy.pdf', 'EXPIRED', 2, 12);

-- ============================================
-- 8. CUSTOMER KYC VERIFICATION
-- ============================================
INSERT INTO customer_kyc_verification (
    user_id,
    sumsub_applicant_id,
    verification_level,
    status,
    reason,
    risk_level,
    submitted_at,
    reviewed_at,
    approved_at,
    created_by
) VALUES
-- Approved verifications
(1, 'SUMSUB_APP_001', 'BASIC', 'APPROVED', 'All documents verified', 'LOW', 
 '2026-01-10 10:00:00', '2026-01-11 15:30:00', '2026-01-11 15:30:00', 2),
(2, 'SUMSUB_APP_002', 'ENHANCED', 'APPROVED', 'Enhanced due diligence completed', 'LOW',
 '2026-01-15 09:00:00', '2026-01-16 14:00:00', '2026-01-16 14:00:00', 2),
(3, 'SUMSUB_APP_003', 'ENHANCED', 'APPROVED', 'Compliance officer verified', 'LOW',
 '2026-01-20 11:30:00', '2026-01-21 16:00:00', '2026-01-21 16:00:00', 3),
(4, 'SUMSUB_APP_004', 'BASIC', 'APPROVED', 'Standard verification passed', 'LOW',
 '2026-02-01 08:00:00', '2026-02-02 10:00:00', '2026-02-02 10:00:00', 2),
(6, 'SUMSUB_APP_006', 'BASIC', 'APPROVED', 'Business owner verification', 'LOW',
 '2026-02-10 13:00:00', '2026-02-11 09:30:00', '2026-02-11 09:30:00', 3),
(7, 'SUMSUB_APP_007', 'ENHANCED', 'APPROVED', 'High value customer approved', 'MEDIUM',
 '2026-02-15 10:00:00', '2026-02-16 11:00:00', '2026-02-16 11:00:00', 2),

-- Pending verifications
(8, 'SUMSUB_APP_008', 'BASIC', 'PENDING', 'Awaiting document review', NULL,
 '2026-03-01 14:00:00', NULL, NULL, 8),
(9, 'SUMSUB_APP_009', 'BASIC', 'PENDING', 'Under review', NULL,
 '2026-03-03 09:00:00', NULL, NULL, 9),

-- In review
(10, 'SUMSUB_APP_010', 'ENHANCED', 'IN_REVIEW', 'Complex case under investigation', 'MEDIUM',
 '2026-02-25 11:00:00', '2026-03-01 10:00:00', NULL, 10),

-- Rejected verification
(11, 'SUMSUB_APP_011', 'BASIC', 'REJECTED', 'Document quality insufficient', 'HIGH',
 '2026-02-20 16:00:00', '2026-02-21 09:00:00', NULL, 11);

-- ============================================
-- 9. AML SCREENING RESULTS
-- ============================================
INSERT INTO aml_screening_results (
    verification_id,
    user_id,
    screening_type,
    match_found,
    risk_score,
    match_details,
    screened_at,
    created_by
) VALUES
-- Clean screenings (no matches)
(1, 1, 'PEP', FALSE, 0, '{"matches": [], "screening_date": "2026-01-11"}', '2026-01-11 15:00:00', 2),
(1, 1, 'SANCTIONS', FALSE, 0, '{"matches": [], "screening_date": "2026-01-11"}', '2026-01-11 15:05:00', 2),
(1, 1, 'ADVERSE_MEDIA', FALSE, 5, '{"matches": [], "screening_date": "2026-01-11", "notes": "Minor alerts cleared"}', '2026-01-11 15:10:00', 2),

(2, 2, 'PEP', FALSE, 0, '{"matches": [], "screening_date": "2026-01-16"}', '2026-01-16 13:30:00', 2),
(2, 2, 'SANCTIONS', FALSE, 0, '{"matches": [], "screening_date": "2026-01-16"}', '2026-01-16 13:35:00', 2),
(2, 2, 'ADVERSE_MEDIA', FALSE, 0, '{"matches": [], "screening_date": "2026-01-16"}', '2026-01-16 13:40:00', 2),

(3, 3, 'PEP', FALSE, 0, '{"matches": [], "screening_date": "2026-01-21"}', '2026-01-21 15:30:00', 3),
(3, 3, 'SANCTIONS', FALSE, 0, '{"matches": [], "screening_date": "2026-01-21"}', '2026-01-21 15:35:00', 3),

-- Some matches found (medium risk)
(7, 7, 'PEP', FALSE, 0, '{"matches": [], "screening_date": "2026-02-16"}', '2026-02-16 10:30:00', 2),
(7, 7, 'SANCTIONS', FALSE, 0, '{"matches": [], "screening_date": "2026-02-16"}', '2026-02-16 10:35:00', 2),
(7, 7, 'ADVERSE_MEDIA', TRUE, 45, 
 '{"matches": [{"source": "News Archive", "date": "2020-05-15", "relevance": "Low", "description": "Minor business dispute resolved"}], "screening_date": "2026-02-16"}', 
 '2026-02-16 10:40:00', 2),

-- High risk case
(10, 10, 'PEP', TRUE, 75, 
 '{"matches": [{"source": "PEP Database", "name": "Similar Name Match", "relevance": "Medium", "notes": "Requires further investigation"}], "screening_date": "2026-03-01"}',
 '2026-03-01 10:15:00', 2),
(10, 10, 'SANCTIONS', FALSE, 0, '{"matches": [], "screening_date": "2026-03-01"}', '2026-03-01 10:20:00', 2),
(10, 10, 'ADVERSE_MEDIA', TRUE, 60,
 '{"matches": [{"source": "Financial News", "date": "2023-08-10", "relevance": "Medium", "description": "Investigation mentioned in article"}], "screening_date": "2026-03-01"}',
 '2026-03-01 10:25:00', 2);

-- ============================================
-- 10. QUESTIONNAIRE QUESTIONS
-- ============================================
INSERT INTO questionnaire_questions (
    question_text,
    question_category,
    display_order,
    status,
    created_by
) VALUES
-- Business Purpose
('What is the primary purpose of your business?', 'BUSINESS_PURPOSE', 1, 'ACTIVE', 1),
('Which countries will you send money to most frequently?', 'BUSINESS_PURPOSE', 2, 'ACTIVE', 1),
('What is your expected monthly transaction volume?', 'BUSINESS_PURPOSE', 3, 'ACTIVE', 1),
('What is your expected average transaction size?', 'BUSINESS_PURPOSE', 4, 'ACTIVE', 1),

-- Source of Funds
('What is the source of funds for your transactions?', 'SOURCE_OF_FUNDS', 5, 'ACTIVE', 1),
('Do you receive funds from third parties?', 'SOURCE_OF_FUNDS', 6, 'ACTIVE', 1),
('How do you receive payments from customers?', 'SOURCE_OF_FUNDS', 7, 'ACTIVE', 1),

-- Business Operations
('How long has your business been operating?', 'BUSINESS_OPERATIONS', 8, 'ACTIVE', 1),
('How many employees does your business have?', 'BUSINESS_OPERATIONS', 9, 'ACTIVE', 1),
('Do you have a physical office location?', 'BUSINESS_OPERATIONS', 10, 'ACTIVE', 1),
('What is your business website URL?', 'BUSINESS_OPERATIONS', 11, 'ACTIVE', 1),

-- Compliance
('Do you have Anti-Money Laundering policies in place?', 'COMPLIANCE', 12, 'ACTIVE', 1),
('Who is your Money Laundering Reporting Officer (MLRO)?', 'COMPLIANCE', 13, 'ACTIVE', 1),
('Do you conduct customer due diligence checks?', 'COMPLIANCE', 14, 'ACTIVE', 1),
('How do you identify and verify your customers?', 'COMPLIANCE', 15, 'ACTIVE', 1),

-- Risk Assessment
('Have you ever been subject to any regulatory sanctions?', 'RISK_ASSESSMENT', 16, 'ACTIVE', 1),
('Do you deal with high-risk jurisdictions?', 'RISK_ASSESSMENT', 17, 'ACTIVE', 1),
('Do you handle transactions for politically exposed persons (PEPs)?', 'RISK_ASSESSMENT', 18, 'ACTIVE', 1),

-- Additional
('How did you hear about our services?', 'ADDITIONAL', 19, 'ACTIVE', 1),
('What payment methods do your customers use?', 'ADDITIONAL', 20, 'ACTIVE', 1);

-- ============================================
-- 11. CUSTOMER ANSWERS
-- ============================================
INSERT INTO customer_answers (
    user_id,
    question_id,
    answer,
    created_by
) VALUES
-- Answers from User 6 (Olivia - DigiPay UK owner)
(6, 1, 'International money transfer and remittance services', 6),
(6, 2, 'United States, Germany, France, Spain', 6),
(6, 3, '£100,000 - £250,000', 6),
(6, 4, '£500 - £2,000', 6),
(6, 5, 'Business revenue from customer fees', 6),
(6, 6, 'No, direct payments only', 6),
(6, 7, 'Bank transfer, debit card, and mobile payment apps', 6),
(6, 8, '4 years', 6),
(6, 9, '15 employees', 6),
(6, 10, 'Yes, in Manchester', 6),
(6, 11, 'www.digipayuk.com', 6),
(6, 12, 'Yes, comprehensive AML/CTF policies', 6),
(6, 13, 'Sarah Williams (external consultant)', 6),
(6, 14, 'Yes, for all new customers', 6),
(6, 15, 'Electronic ID verification via Sumsub', 6),
(6, 16, 'No', 6),
(6, 17, 'No, we only serve low-risk EU and US markets', 6),
(6, 18, 'No PEP transactions', 6),
(6, 19, 'Industry referral', 6),
(6, 20, 'Bank transfer (70%), debit cards (25%), mobile wallets (5%)', 6),

-- Answers from User 7 (James - GlobalTransfer owner)
(7, 1, 'Cross-border payment processing for e-commerce', 7),
(7, 2, 'India, United Arab Emirates, Philippines, Nigeria', 7),
(7, 3, '£1,000,000 - £5,000,000', 7),
(7, 4, '£1,000 - £10,000', 7),
(7, 5, 'E-commerce merchant payments', 7),
(7, 6, 'Yes, from payment aggregators', 7),
(7, 7, 'API integration with merchant platforms', 7),
(7, 8, '8 years', 7),
(7, 9, '50+ employees', 7),
(7, 10, 'Yes, headquarters in London with branches in Birmingham and Edinburgh', 7),
(7, 11, 'www.globaltransfer.co.uk', 7),
(7, 12, 'Yes, FCA regulated with full compliance program', 7),
(7, 13, 'John Smith (in-house MLRO)', 7),
(7, 14, 'Yes, risk-based approach with enhanced DD for high-risk customers', 7),
(7, 15, 'Multi-stage verification: document scan, biometric check, database screening', 7),
(7, 16, 'No regulatory sanctions', 7),
(7, 17, 'Yes, we have enhanced procedures for high-risk corridors', 7),
(7, 18, 'Yes, with enhanced due diligence procedures', 7),
(7, 19, 'Financial industry conference', 7),
(7, 20, 'API (60%), bank transfer (30%), card payments (10%)', 7),

-- Partial answers from User 8 (Sophia - startup)
(8, 1, 'Mobile payment solution for small businesses', 8),
(8, 2, 'United Kingdom only (domestic)', 8),
(8, 3, 'Under £100,000', 8),
(8, 4, '£50 - £500', 8),
(8, 8, 'Less than 1 year', 8),
(8, 9, '5 employees', 8),
(8, 10, 'Remote-only business', 8),
(8, 19, 'Online search', 8);

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Show counts of all data
-- SELECT 'Permissions' as TableName, COUNT(*) as RecordCount FROM permissions
-- UNION ALL
-- SELECT 'Roles', COUNT(*) FROM roles
-- UNION ALL
-- SELECT 'Role_Permissions', COUNT(*) FROM role_permissions
-- UNION ALL
-- SELECT 'Users', COUNT(*) FROM users
-- UNION ALL
-- SELECT 'Addresses', COUNT(*) FROM address
-- UNION ALL
-- SELECT 'Organisations', COUNT(*) FROM organisation
-- UNION ALL
-- SELECT 'KYC_Documents', COUNT(*) FROM kyc_documents
-- UNION ALL
-- SELECT 'Customer_KYC_Verification', COUNT(*) FROM customer_kyc_verification
-- UNION ALL
-- SELECT 'AML_Screening_Results', COUNT(*) FROM aml_screening_results
-- UNION ALL
-- SELECT 'Questionnaire_Questions', COUNT(*) FROM questionnaire_questions
-- UNION ALL
-- SELECT 'Customer_Answers', COUNT(*) FROM customer_answers;

-- ============================================
-- TEST DATA SUMMARY
-- ============================================
-- 17 Permissions
-- 4 Roles  
-- ~30 Role-Permission mappings
-- 12 Users (1 admin, 2 compliance, 2 staff, 7 regular users)
-- 15 Addresses (business, registered, correspondence, residential)
-- 8 Organisations (various types and statuses)
-- 16 KYC Documents (various statuses)
-- 10 KYC Verifications (approved, pending, in review, rejected)
-- 14 AML Screening Results (clean, medium, high risk)
-- 20 Questionnaire Questions (5 categories)
-- 48 Customer Answers (from 3 different users)
-- ============================================

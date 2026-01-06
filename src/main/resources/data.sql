-- Insert default permissions
INSERT INTO Permissions (Permission_Name, Description, Resource, Action) VALUES ('USER_READ', 'Read user information', 'users', 'read');
INSERT INTO Permissions (Permission_Name, Description, Resource, Action) VALUES ('USER_WRITE', 'Create and update users', 'users', 'write');
INSERT INTO Permissions (Permission_Name, Description, Resource, Action) VALUES ('CUSTOMER_READ', 'Read customer information', 'customers', 'read');
INSERT INTO Permissions (Permission_Name, Description, Resource, Action) VALUES ('CUSTOMER_WRITE', 'Create and update customers', 'customers', 'write');
INSERT INTO Permissions (Permission_Name, Description, Resource, Action) VALUES ('ORG_READ', 'Read organisation information', 'organisations', 'read');
INSERT INTO Permissions (Permission_Name, Description, Resource, Action) VALUES ('ORG_WRITE', 'Create and update organisations', 'organisations', 'write');
INSERT INTO Permissions (Permission_Name, Description, Resource, Action) VALUES ('KYC_READ', 'Read KYC documents', 'kyc', 'read');
INSERT INTO Permissions (Permission_Name, Description, Resource, Action) VALUES ('KYC_WRITE', 'Upload and update KYC documents', 'kyc', 'write');
INSERT INTO Permissions (Permission_Name, Description, Resource, Action) VALUES ('KYC_VERIFY', 'Verify KYC documents', 'kyc', 'verify');

-- Insert default roles
INSERT INTO Roles (Role_Name, Role_Description) VALUES ('SYSTEM_ADMINISTRATOR', 'Full system access');
INSERT INTO Roles (Role_Name, Role_Description) VALUES ('ADMIN', 'Administrator with user management capabilities');
INSERT INTO Roles (Role_Name, Role_Description) VALUES ('COMPLIANCE_OFFICER', 'Compliance and AML access');
INSERT INTO Roles (Role_Name, Role_Description) VALUES ('OPERATIONAL_STAFF', 'Operational access');

-- Link permissions to roles
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (1, 1);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (1, 2);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (1, 3);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (1, 4);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (1, 5);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (1, 6);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (1, 7);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (1, 8);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (1, 9);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (2, 1);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (2, 3);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (2, 5);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (2, 7);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (3, 1);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (3, 3);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (3, 5);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (3, 6);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (3, 7);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (3, 8);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (3, 9);

-- Insert default admin user
INSERT INTO Users (Phone_Number, Email, Role_Identifier, First_Name, Middle_Name, Last_Name, Date_Of_Birth, Status_Description) 
VALUES ('+1234567890', 'admin@fincore.com', 1, 'System', NULL, 'Administrator', '1990-01-01', 'ACTIVE');

-- Insert compliance officer user
INSERT INTO Users (Phone_Number, Email, Role_Identifier, First_Name, Middle_Name, Last_Name, Date_Of_Birth, Status_Description) 
VALUES ('+1234567891', 'compliance@fincore.com', 3, 'Compliance', NULL, 'Officer', '1985-05-15', 'ACTIVE');

-- Insert operational staff user
INSERT INTO Users (Phone_Number, Email, Role_Identifier, First_Name, Middle_Name, Last_Name, Date_Of_Birth, Status_Description) 
VALUES ('+1234567892', 'staff@fincore.com', 4, 'Operational', NULL, 'Staff', '1992-03-20', 'ACTIVE');

-- ============================================
-- Phase 2: Organisation Sample Data
-- ============================================

-- Insert sample addresses
INSERT INTO Address (Type_Code, Address_Line1, Address_Line2, Postal_Code, State_Code, City, Country, Status_Description, Created_By) 
VALUES (3, '10 Downing Street', 'Westminster', 'SW1A 2AA', 'Greater London', 'London', 'United Kingdom', 'ACTIVE', 1);

INSERT INTO Address (Type_Code, Address_Line1, Address_Line2, Postal_Code, State_Code, City, Country, Status_Description, Created_By) 
VALUES (2, '1 Canada Square', 'Canary Wharf', 'E14 5AB', 'Greater London', 'London', 'United Kingdom', 'ACTIVE', 1);

INSERT INTO Address (Type_Code, Address_Line1, Address_Line2, Postal_Code, State_Code, City, Country, Status_Description, Created_By) 
VALUES (4, 'PO Box 1234', NULL, 'EC1A 1AA', 'Greater London', 'London', 'United Kingdom', 'ACTIVE', 1);

-- Insert sample organisation
INSERT INTO Organisation (
    User_Identifier, Registration_Number, SIC_Code, Legal_Name, Business_Name, 
    Organisation_Type_Description, Business_Description, Incorporation_Date, Country_Of_Incorporation,
    HMRC_MLR_Number, FCA_Number, Number_Of_Branches, Number_Of_Agents,
    Company_Number, Website_Address, Primary_Remittance_Destination_Country, 
    Monthly_Turnover_Range, Number_Of_Incoming_Transactions, Number_Of_Outgoing_Transactions,
    Registered_Address_Identifier, Business_Address_Identifier, Correspondence_Address_Identifier,
    Status_Description, Created_By
) VALUES (
    1, 'REG12345678', '64999', 'Fincore Money Services Ltd', 'Fincore Remittance',
    'LTD', 'International money transfer and remittance services', '2018-06-15', 'United Kingdom',
    'XMLR00123456', 'FRN123456', '5', '20',
    'CN12345678', 'https://fincore-remittance.com', 'India',
    '1000000-5000000', '5000', '4500',
    1, 2, 3,
    'ACTIVE', 1
);

-- Insert sample KYC documents for the organisation
INSERT INTO KYC_Documents (Reference_Identifier, Document_Type_Description, File_Name, File_URL, Status_Description, Created_By)
VALUES (1, 'CERTIFICATE_OF_INCORPORATION', 'fincore_certificate_of_incorporation.pdf', 'https://storage.fincore.com/docs/cert_incorporation.pdf', 'VERIFIED', 1);

INSERT INTO KYC_Documents (Reference_Identifier, Document_Type_Description, File_Name, File_URL, Status_Description, Created_By)
VALUES (1, 'HMRC_REGISTRATION', 'fincore_hmrc_registration.pdf', 'https://storage.fincore.com/docs/hmrc_reg.pdf', 'VERIFIED', 1);

INSERT INTO KYC_Documents (Reference_Identifier, Document_Type_Description, File_Name, File_URL, Status_Description, Created_By)
VALUES (1, 'FCA_AUTHORISATION', 'fincore_fca_authorisation.pdf', 'https://storage.fincore.com/docs/fca_auth.pdf', 'PENDING', 1);

INSERT INTO KYC_Documents (Reference_Identifier, Document_Type_Description, File_Name, File_URL, Status_Description, Created_By)
VALUES (1, 'PROOF_OF_ADDRESS', 'fincore_utility_bill.pdf', 'https://storage.fincore.com/docs/utility_bill.pdf', 'VERIFIED', 1);


-- Insert default permissions
INSERT INTO Permissions (Permission_Name, Description, Resource, Action) VALUES ('USER_READ', 'Read user information', 'users', 'read');
INSERT INTO Permissions (Permission_Name, Description, Resource, Action) VALUES ('USER_WRITE', 'Create and update users', 'users', 'write');
INSERT INTO Permissions (Permission_Name, Description, Resource, Action) VALUES ('CUSTOMER_READ', 'Read customer information', 'customers', 'read');
INSERT INTO Permissions (Permission_Name, Description, Resource, Action) VALUES ('CUSTOMER_WRITE', 'Create and update customers', 'customers', 'write');

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
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (2, 1);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (2, 3);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (3, 1);
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES (3, 3);

-- Insert default admin user
INSERT INTO Users (Phone_Number, Email, Role_Identifier, First_Name, Middle_Name, Last_Name, Date_Of_Birth, Status_Description) 
VALUES ('+1234567890', 'admin@fincore.com', 1, 'System', NULL, 'Administrator', '1990-01-01', 'ACTIVE');

-- Insert compliance officer user
INSERT INTO Users (Phone_Number, Email, Role_Identifier, First_Name, Middle_Name, Last_Name, Date_Of_Birth, Status_Description) 
VALUES ('+1234567891', 'compliance@fincore.com', 3, 'Compliance', NULL, 'Officer', '1985-05-15', 'ACTIVE');

-- Insert operational staff user
INSERT INTO Users (Phone_Number, Email, Role_Identifier, First_Name, Middle_Name, Last_Name, Date_Of_Birth, Status_Description) 
VALUES ('+1234567892', 'staff@fincore.com', 4, 'Operational', NULL, 'Staff', '1992-03-20', 'ACTIVE');

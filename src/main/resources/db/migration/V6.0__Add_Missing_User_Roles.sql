-- Add missing user roles required by frontend
-- Migration V6.0

-- Add USER role
INSERT IGNORE INTO Roles (Role_Name, Role_Description) 
VALUES ('USER', 'Standard user with basic access');

-- Add MANAGER role  
INSERT IGNORE INTO Roles (Role_Name, Role_Description) 
VALUES ('MANAGER', 'Manager with operational oversight');

-- Add SUPER_ADMIN role
INSERT IGNORE INTO Roles (Role_Name, Role_Description) 
VALUES ('SUPER_ADMIN', 'Super administrator with elevated privileges');

-- Get role IDs for permission assignments (these will vary by environment)
-- Link permissions to USER role (read-only access)
INSERT IGNORE INTO Role_Permissions (Role_Identifier, Permission_Identifier)
SELECT r.Role_Identifier, p.Permission_Identifier
FROM Roles r, Permissions p
WHERE r.Role_Name = 'USER' 
  AND p.Permission_Name IN ('USER_READ', 'CUSTOMER_READ');

-- Link permissions to MANAGER role (read and some write access)
INSERT IGNORE INTO Role_Permissions (Role_Identifier, Permission_Identifier)
SELECT r.Role_Identifier, p.Permission_Identifier
FROM Roles r, Permissions p
WHERE r.Role_Name = 'MANAGER' 
  AND p.Permission_Name IN ('USER_READ', 'CUSTOMER_READ', 'CUSTOMER_WRITE', 'ORG_READ');

-- Link permissions to SUPER_ADMIN role (all permissions)
INSERT IGNORE INTO Role_Permissions (Role_Identifier, Permission_Identifier)
SELECT r.Role_Identifier, p.Permission_Identifier
FROM Roles r, Permissions p
WHERE r.Role_Name = 'SUPER_ADMIN';

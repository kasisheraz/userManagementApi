-- Create or Update UAT Test User: +447700900000 (Admin)
-- This user is for UAT testing with DevTools
-- Schema: Uses Role_Identifier (1=Admin), no password (OTP auth only)

-- Use INSERT with ON DUPLICATE KEY UPDATE to handle existing user
INSERT INTO Users (
    Phone_Number,
    Email,
    Role_Identifier,
    First_Name,
    Last_Name,
    Status_Description
) VALUES (
    '+447700900000',
    'admin@fincore.test',
    1,
    'Admin',
    'User',
    'Active'
)
ON DUPLICATE KEY UPDATE
    Email = 'admin@fincore.test',
    Role_Identifier = 1,
    First_Name = 'Admin',
    Last_Name = 'User',
    Status_Description = 'Active',
    Last_Modified_Datetime = CURRENT_TIMESTAMP;

-- Verify the user was created/updated
SELECT 
    User_Identifier,
    Phone_Number,
    Email,
    First_Name,
    Last_Name,
    Role_Identifier,
    Status_Description,
    Created_Datetime
FROM Users 
WHERE Phone_Number = '+447700900000';

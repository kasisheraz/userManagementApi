-- Cloud SQL Permission Fix for fincore_app user
-- This script grants all necessary permissions for the User Management API

-- First, let's ensure the user exists (it should already exist)
-- CREATE USER IF NOT EXISTS 'fincore_app'@'%' IDENTIFIED BY 'FinCore2024Secure';

-- Grant all privileges on the my_auth_db database
GRANT ALL PRIVILEGES ON my_auth_db.* TO 'fincore_app'@'%';

-- Grant specific privileges that might be needed
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, INDEX, ALTER ON my_auth_db.* TO 'fincore_app'@'%';

-- Also grant privileges for the cloudsqlproxy connections (this is the key fix)
GRANT ALL PRIVILEGES ON my_auth_db.* TO 'fincore_app'@'cloudsqlproxy~%';

-- Grant connection privileges
GRANT USAGE ON *.* TO 'fincore_app'@'%';
GRANT USAGE ON *.* TO 'fincore_app'@'cloudsqlproxy~%';

-- Flush privileges to ensure changes take effect
FLUSH PRIVILEGES;

-- Show the grants for verification
SHOW GRANTS FOR 'fincore_app'@'%';
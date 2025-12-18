-- Grant necessary permissions to fincore_app user
USE my_auth_db;

-- Show current grants for fincore_app
SHOW GRANTS FOR 'fincore_app'@'%';

-- Grant all privileges on the my_auth_db database to fincore_app user
GRANT ALL PRIVILEGES ON my_auth_db.* TO 'fincore_app'@'%';

-- Grant SELECT, INSERT, UPDATE, DELETE privileges specifically
GRANT SELECT, INSERT, UPDATE, DELETE ON my_auth_db.* TO 'fincore_app'@'%';

-- Flush privileges to apply changes
FLUSH PRIVILEGES;

-- Verify the grants
SHOW GRANTS FOR 'fincore_app'@'%';
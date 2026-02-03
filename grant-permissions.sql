-- Grant REFERENCES permission to fincore_app user
-- This is required for creating foreign key constraints
-- Execute this on Cloud SQL instance: fincore-npe-db

GRANT REFERENCES ON fincore_db.* TO 'fincore_app'@'%';

-- Also grant other potentially needed permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON fincore_db.* TO 'fincore_app'@'%';

-- Apply the changes
FLUSH PRIVILEGES;

-- Verify the grants
SHOW GRANTS FOR 'fincore_app'@'%';

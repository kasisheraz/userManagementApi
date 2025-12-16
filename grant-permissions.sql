-- Grant permissions to fincore_app user for my_auth_db
GRANT ALL PRIVILEGES ON my_auth_db.* TO 'fincore_app'@'%';
FLUSH PRIVILEGES;

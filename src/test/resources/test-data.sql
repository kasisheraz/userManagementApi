-- Insert default permissions
INSERT INTO permissions (name, description, module) VALUES ('USER_READ', 'Read user information', 'USER_MANAGEMENT');
INSERT INTO permissions (name, description, module) VALUES ('USER_WRITE', 'Create and update users', 'USER_MANAGEMENT');
INSERT INTO permissions (name, description, module) VALUES ('CUSTOMER_READ', 'Read customer information', 'CUSTOMER_MANAGEMENT');
INSERT INTO permissions (name, description, module) VALUES ('CUSTOMER_WRITE', 'Create and update customers', 'CUSTOMER_MANAGEMENT');

-- Insert default roles
INSERT INTO roles (name, description) VALUES ('SYSTEM_ADMINISTRATOR', 'Full system access');
INSERT INTO roles (name, description) VALUES ('COMPLIANCE_OFFICER', 'Compliance and AML access');
INSERT INTO roles (name, description) VALUES ('OPERATIONAL_STAFF', 'Operational access');

-- Link permissions to roles
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 1);
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 2);
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 3);
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 4);
INSERT INTO role_permissions (role_id, permission_id) VALUES (2, 1);
INSERT INTO role_permissions (role_id, permission_id) VALUES (2, 3);
INSERT INTO role_permissions (role_id, permission_id) VALUES (3, 1);
INSERT INTO role_permissions (role_id, permission_id) VALUES (3, 3);

-- Insert default admin user (password: Admin@123456)
INSERT INTO users (username, password, full_name, email, phone_number, employee_id, department, job_title, status, role_id, failed_login_attempts, created_at, updated_at) 
VALUES ('admin', '$2a$10$nZhPCqOb6NsCCXmFPjscGeaH83R4HOBY0o8FMJvfxAkyfF22PDC12', 'System Administrator', 'admin@fincore.com', '+1234567890', 'EMP001', 'IT', 'System Admin', 'ACTIVE', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert compliance officer user (password: Compliance@123)
INSERT INTO users (username, password, full_name, email, phone_number, employee_id, department, job_title, status, role_id, failed_login_attempts, created_at, updated_at) 
VALUES ('compliance', '$2a$10$1My2JRg0A4A0PGwzalpd2efAY3Evz87X3CQSQ9SrvExqQSj6.E7O.', 'Compliance Officer', 'compliance@fincore.com', '+1234567891', 'EMP002', 'Compliance', 'Compliance Officer', 'ACTIVE', 2, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert operational staff user (password: Staff@123456)
INSERT INTO users (username, password, full_name, email, phone_number, employee_id, department, job_title, status, role_id, failed_login_attempts, created_at, updated_at) 
VALUES ('staff', '$2a$10$OHPlw5KVcthAjlsaDwzRmueQMvzxEVfnPt2KcT52cmYciyW6KijdC', 'Operational Staff', 'staff@fincore.com', '+1234567892', 'EMP003', 'Operations', 'Staff Member', 'ACTIVE', 3, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

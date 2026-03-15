-- ============================================
-- IMES SYSTEM - TEST USERS SQL SCRIPT
-- ============================================
-- Generated: 2026-02-28
-- BCrypt hashes generated using Spring Boot BCryptPasswordEncoder
-- See TEST_ACCOUNTS.txt for plaintext credentials
-- ============================================

-- Clear existing users first
DELETE FROM users;

-- Password: admin123
INSERT INTO users (id, email, password, full_name, phone_number, role, is_active, created_at, updated_at) 
VALUES (1, 'admin@imes.com', '$2a$10$URlQbHFVC30v0VYbSYZnlet5t3khkbx.EBzZ36kgEJH20ChNzlQ/2', 'System Administrator', '0901234567', 'ADMIN', true, NOW(), NOW());

-- Password: hr123
INSERT INTO users (id, email, password, full_name, phone_number, role, is_active, created_at, updated_at) 
VALUES (2, 'hr@imes.com', '$2a$10$sCgghsZjPMj3Q85DZc3tjux573H.Qzhpbpimr7VM8bNVmY/2ot852', 'HR Manager', '0901234568', 'HR', true, NOW(), NOW());

-- Password: mentor123
INSERT INTO users (id, email, password, full_name, phone_number, role, is_active, created_at, updated_at) 
VALUES (3, 'mentor@imes.com', '$2a$10$lpPCRY3.r6gVjyggbwqXJevpXTHO2gXNfCqL0r0RjL3mVIO1mTsI2', 'Senior Mentor', '0901234569', 'MENTOR', true, NOW(), NOW());

-- Password: intern123
INSERT INTO users (id, email, password, full_name, phone_number, role, is_active, created_at, updated_at) 
VALUES (4, 'intern@imes.com', '$2a$10$.A56LxiSyLaq6DpiyEmdKOjLDg29KNRZFsLUXpAiJf.YeVG0g0vNK', 'Test Intern', '0901234570', 'INTERN', true, NOW(), NOW());

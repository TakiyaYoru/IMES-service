-- liquibase formatted sql

-- changeset imes:v2.2.1
-- comment: Add student_id, mentor_id, department_id to intern_profiles table
ALTER TABLE intern_profiles ADD COLUMN IF NOT EXISTS student_id VARCHAR(50);
ALTER TABLE intern_profiles ADD COLUMN IF NOT EXISTS mentor_id BIGINT;
ALTER TABLE intern_profiles ADD COLUMN IF NOT EXISTS department_id BIGINT;
ALTER TABLE intern_profiles ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500);

-- Add unique constraint for student_id
ALTER TABLE intern_profiles ADD CONSTRAINT uk_intern_profiles_student_id UNIQUE (student_id);

-- Add foreign key constraints
ALTER TABLE intern_profiles ADD CONSTRAINT fk_intern_profiles_mentor 
    FOREIGN KEY (mentor_id) REFERENCES users(id);
    
ALTER TABLE intern_profiles ADD CONSTRAINT fk_intern_profiles_department 
    FOREIGN KEY (department_id) REFERENCES departments(id);

-- rollback ALTER TABLE intern_profiles DROP CONSTRAINT IF EXISTS fk_intern_profiles_department;
-- rollback ALTER TABLE intern_profiles DROP CONSTRAINT IF EXISTS fk_intern_profiles_mentor;
-- rollback ALTER TABLE intern_profiles DROP CONSTRAINT IF EXISTS uk_intern_profiles_student_id;
-- rollback ALTER TABLE intern_profiles DROP COLUMN IF EXISTS avatar_url;
-- rollback ALTER TABLE intern_profiles DROP COLUMN IF EXISTS department_id;
-- rollback ALTER TABLE intern_profiles DROP COLUMN IF EXISTS mentor_id;
-- rollback ALTER TABLE intern_profiles DROP COLUMN IF EXISTS student_id;

-- changeset imes:v2.2.2
-- comment: Add indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_intern_profiles_mentor ON intern_profiles(mentor_id);
CREATE INDEX IF NOT EXISTS idx_intern_profiles_department ON intern_profiles(department_id);
CREATE INDEX IF NOT EXISTS idx_intern_profiles_status ON intern_profiles(status);
CREATE INDEX IF NOT EXISTS idx_intern_profiles_student_id ON intern_profiles(student_id);

-- rollback DROP INDEX IF EXISTS idx_intern_profiles_student_id;
-- rollback DROP INDEX IF EXISTS idx_intern_profiles_status;
-- rollback DROP INDEX IF EXISTS idx_intern_profiles_department;
-- rollback DROP INDEX IF EXISTS idx_intern_profiles_mentor;

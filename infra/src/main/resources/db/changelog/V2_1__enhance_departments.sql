--liquibase formatted sql

--changeset imes:add_department_enhancements
ALTER TABLE departments ADD COLUMN IF NOT EXISTS code VARCHAR(50);
ALTER TABLE departments ADD COLUMN IF NOT EXISTS manager_id BIGINT;
ALTER TABLE departments ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;

--rollback ALTER TABLE departments DROP COLUMN IF EXISTS code;
--rollback ALTER TABLE departments DROP COLUMN IF EXISTS manager_id;
--rollback ALTER TABLE departments DROP COLUMN IF EXISTS is_active;

--changeset imes:add_department_constraints
ALTER TABLE departments ADD CONSTRAINT IF NOT EXISTS uk_departments_code UNIQUE (code);
ALTER TABLE departments ADD CONSTRAINT IF NOT EXISTS fk_departments_manager FOREIGN KEY (manager_id) REFERENCES users(id);
CREATE INDEX IF NOT EXISTS idx_departments_active ON departments(is_active);
CREATE INDEX IF NOT EXISTS idx_departments_name ON departments(name);

--rollback DROP INDEX IF EXISTS idx_departments_name;
--rollback DROP INDEX IF EXISTS idx_departments_active;
--rollback ALTER TABLE departments DROP CONSTRAINT IF EXISTS fk_departments_manager;
--rollback ALTER TABLE departments DROP CONSTRAINT IF EXISTS uk_departments_code;

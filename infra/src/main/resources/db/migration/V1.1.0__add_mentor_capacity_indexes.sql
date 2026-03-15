-- ============================================
-- Add Active Interns Count Support for Mentor Capacity
-- ============================================
-- Version: 1.1.0
-- Date: 2026-02-28
-- Description: Add indexes to support mentor capacity validation
-- ============================================

-- Add index on mentor_id for counting active interns
CREATE INDEX IF NOT EXISTS idx_mentor_assignments_mentor_id_active 
ON mentor_assignments(mentor_id, is_active);

-- Add composite index for faster lookups
CREATE INDEX IF NOT EXISTS idx_mentor_assignments_mentor_intern 
ON mentor_assignments(mentor_id, intern_profile_id, is_active);

COMMENT ON INDEX idx_mentor_assignments_mentor_id_active IS 'For mentor capacity validation (max 5 interns)';

-- ============================================
-- Add Feedback and Late Submission Support to Submissions Table
-- ============================================
-- Version: 1.2.0
-- Date: 2026-02-28
-- Description: Add mentor feedback, scoring, and late submission tracking
-- ============================================

-- Add feedback and late submission columns to submissions table
ALTER TABLE submissions 
ADD COLUMN IF NOT EXISTS score DECIMAL(4,2),
ADD COLUMN IF NOT EXISTS mentor_comments TEXT,
ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS reviewed_by BIGINT,
ADD COLUMN IF NOT EXISTS is_late BOOLEAN NOT NULL DEFAULT FALSE;

-- Add check constraint for score (0-10)
ALTER TABLE submissions 
ADD CONSTRAINT check_score_range CHECK (score IS NULL OR (score >= 0 AND score <= 10));

-- Add index on reviewed_by for performance
CREATE INDEX IF NOT EXISTS idx_submissions_reviewed_by ON submissions(reviewed_by);

-- Add index on is_late for filtering
CREATE INDEX IF NOT EXISTS idx_submissions_is_late ON submissions(is_late);

COMMENT ON COLUMN submissions.score IS 'Mentor score (0-10)';
COMMENT ON COLUMN submissions.mentor_comments IS 'Mentor feedback comments';
COMMENT ON COLUMN submissions.reviewed_at IS 'When mentor reviewed the submission';
COMMENT ON COLUMN submissions.reviewed_by IS 'Mentor ID who reviewed';
COMMENT ON COLUMN submissions.is_late IS 'Whether submission was after deadline';

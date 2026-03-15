-- Migration: Create refresh_tokens table for JWT refresh token management
-- Author: IMES Team
-- Date: 2026-03-04

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP,
    device_info VARCHAR(255),
    ip_address VARCHAR(50),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_refresh_token_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_token_user_active ON refresh_tokens(user_id, revoked_at);
CREATE INDEX IF NOT EXISTS idx_refresh_token_expires ON refresh_tokens(expires_at);

-- Comments
COMMENT ON TABLE refresh_tokens IS 'Stores JWT refresh tokens for secure authentication';
COMMENT ON COLUMN refresh_tokens.token IS 'Unique refresh token string (UUID or JWT)';
COMMENT ON COLUMN refresh_tokens.expires_at IS 'Token expiration timestamp (typically 7 days from creation)';
COMMENT ON COLUMN refresh_tokens.revoked_at IS 'Timestamp when token was revoked (NULL if still active)';
COMMENT ON COLUMN refresh_tokens.device_info IS 'User agent or device identifier';
COMMENT ON COLUMN refresh_tokens.ip_address IS 'IP address from which token was generated';

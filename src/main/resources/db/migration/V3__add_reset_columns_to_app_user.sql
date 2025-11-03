ALTER TABLE app_user
ADD COLUMN IF NOT EXISTS reset_token_sha256           TEXT,
ADD COLUMN IF NOT EXISTS reset_token_expires_at       TIMESTAMPTZ,
ADD COLUMN IF NOT EXISTS reset_token_used             BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS reset_request_ip             TEXT,
ADD COLUMN IF NOT EXISTS reset_user_agent             TEXT,
ADD COLUMN IF NOT EXISTS reset_created_at             TIMESTAMPTZ NOT NULL DEFAULT now();

CREATE INDEX IF NOT EXISTS idx_app_user_reset_token_sha256
  ON app_user (reset_token_sha256);

CREATE INDEX IF NOT EXISTS idx_app_user_reset_token_expires_at
  ON app_user (reset_token_expires_at);

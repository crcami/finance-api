CREATE INDEX IF NOT EXISTS idx_record_user_due_date ON record (user_id, due_date);
CREATE INDEX IF NOT EXISTS idx_record_user_status   ON record (user_id, status);
CREATE INDEX IF NOT EXISTS idx_record_user_kind     ON record (user_id, kind);

CREATE INDEX IF NOT EXISTS idx_record_user_category ON record (user_id, category_id);

CREATE INDEX IF NOT EXISTS idx_refresh_valid ON refresh_token (user_id, token_hash) WHERE revoked = FALSE;

CREATE INDEX IF NOT EXISTS idx_category_user_archived ON category (user_id, archived);


CREATE EXTENSION IF NOT EXISTS pgcrypto;


CREATE TABLE IF NOT EXISTS app_user (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email        VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  full_name    VARCHAR(120) NOT NULL,
  created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);


CREATE TABLE IF NOT EXISTS refresh_token (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id     UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  token_hash  VARCHAR(64) NOT NULL,
  user_agent  TEXT,
  ip_address  VARCHAR(45),
  expires_at  TIMESTAMPTZ NOT NULL,
  revoked     BOOLEAN NOT NULL DEFAULT FALSE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS category (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id    UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  name       VARCHAR(60) NOT NULL,
  color      VARCHAR(7),
  archived   BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_category_user_lower_name
  ON category (user_id, lower(name));


CREATE TABLE IF NOT EXISTS record (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id      UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  category_id  UUID REFERENCES category(id) ON DELETE SET NULL,
  kind         VARCHAR(16) NOT NULL,   
  status       VARCHAR(16) NOT NULL,   
  amount       NUMERIC(14,2) NOT NULL,
  due_date     DATE NOT NULL,
  paid_at      TIMESTAMPTZ,
  description  VARCHAR(255),
  created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS bulk_request (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id       UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  request_id    VARCHAR(64) NOT NULL,
  created_count INT NOT NULL,
  failed_count  INT NOT NULL,
  processed_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uk_bulk_user_request UNIQUE (user_id, request_id)
);

CREATE INDEX IF NOT EXISTS idx_category_user_id ON category (user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_user_hash ON refresh_token (user_id, token_hash);
CREATE INDEX IF NOT EXISTS idx_record_user_id ON record (user_id);

-- OPTIONAL seed (dev only) - replace the hash if you want a known password.
-- INSERT INTO app_user (email, password_hash, full_name)
-- VALUES ('demo@local', '$2a$12$qy7u7m3K2Pi4b2vF2y3Z6eQm6wq2n0Jc1z9y3C0QkqH6u4uHk7JFu', 'Demo User');

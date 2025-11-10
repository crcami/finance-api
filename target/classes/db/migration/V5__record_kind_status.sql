CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS record_kind (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code       VARCHAR(32) UNIQUE NOT NULL,
  name       VARCHAR(64) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS record_status (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code       VARCHAR(32) UNIQUE NOT NULL,
  name       VARCHAR(64) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO record_kind (code, name)
SELECT v.code, v.name
FROM (VALUES ('income','Entrada'), ('expense','Saída')) AS v(code,name)
LEFT JOIN record_kind k ON k.code = v.code
WHERE k.id IS NULL;

INSERT INTO record_status (code, name)
SELECT v.code, v.name
FROM (VALUES ('pending','Pendente'), ('paid','Pago')) AS v(code,name)
LEFT JOIN record_status s ON s.code = v.code
WHERE s.id IS NULL;

ALTER TABLE record
  ADD COLUMN IF NOT EXISTS kind_id   UUID REFERENCES record_kind(id),
  ADD COLUMN IF NOT EXISTS status_id UUID REFERENCES record_status(id);

UPDATE record r
SET kind_id = k.id
FROM record_kind k
WHERE r.kind = k.code
  AND r.kind_id IS NULL;

UPDATE record r
SET status_id = s.id
FROM record_status s
WHERE r.status = s.code
  AND r.status_id IS NULL;

CREATE INDEX IF NOT EXISTS ix_record_due_date     ON record(due_date);
CREATE INDEX IF NOT EXISTS ix_record_status_id    ON record(status_id);
CREATE INDEX IF NOT EXISTS ix_record_kind_id      ON record(kind_id);
CREATE INDEX IF NOT EXISTS ix_record_category_id  ON record(category_id);

-- db/migration/V4__seed_default_categories.sql

-- Garante o índice único (idempotente)
CREATE UNIQUE INDEX IF NOT EXISTS uk_category_user_lower_name
  ON category (user_id, lower(name));

-- Função que insere categorias padrão para um usuário
CREATE OR REPLACE FUNCTION seed_default_categories(p_user_id uuid)
RETURNS void
LANGUAGE plpgsql
AS $$
DECLARE
  -- (usei nomes sem acento para evitar problemas de encoding no Windows)
  v_defaults CONSTANT jsonb := '[
    {"name":"Alimentacao","color":"#F97316"},
    {"name":"Moradia","color":"#3B82F6"},
    {"name":"Transporte","color":"#10B981"},
    {"name":"Saude","color":"#EF4444"},
    {"name":"Educacao","color":"#22C55E"},
    {"name":"Lazer","color":"#A855F7"},
    {"name":"Contas Fixas","color":"#0EA5E9"},
    {"name":"Salario","color":"#16A34A"}
  ]';
BEGIN
  INSERT INTO category (user_id, name, color, archived)
  SELECT p_user_id, j.name, j.color, false
  FROM jsonb_to_recordset(v_defaults) AS j(name text, color text)
  -- aqui está o ajuste: usa o alvo de conflito por expressão
  ON CONFLICT (user_id, (lower(name))) DO NOTHING;
END;
$$;

-- Trigger para novos usuários
DROP TRIGGER IF EXISTS app_user_seed_cats ON app_user;

CREATE OR REPLACE FUNCTION trg_app_user_seed_cats()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
  PERFORM seed_default_categories(NEW.id);
  RETURN NEW;
END;
$$;

CREATE TRIGGER app_user_seed_cats
AFTER INSERT ON app_user
FOR EACH ROW
EXECUTE FUNCTION trg_app_user_seed_cats();

-- Backfill para usuários existentes
DO $$
DECLARE
  u RECORD;
BEGIN
  FOR u IN SELECT id FROM app_user LOOP
    PERFORM seed_default_categories(u.id);
  END LOOP;
END;
$$;

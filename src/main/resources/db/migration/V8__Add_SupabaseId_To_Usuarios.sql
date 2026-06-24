-- Migración para añadir la columna supabase_id a la tabla usuarios
-- (necesaria para correlacionar con Supabase Auth)

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'usuarios' AND column_name = 'supabase_id') THEN
        ALTER TABLE usuarios ADD COLUMN supabase_id VARCHAR(255);
    END IF;
END $$;

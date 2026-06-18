-- V11: Add full-text search support to posts
-- Add tsvector column for full-text search
ALTER TABLE posts ADD COLUMN IF NOT EXISTS search_vector tsvector;

-- Populate search_vector from existing data
UPDATE posts SET search_vector = 
  setweight(to_tsvector('spanish', coalesce(titulo, '')), 'A') ||
  setweight(to_tsvector('spanish', coalesce(contenido, '')), 'B');

-- Create GIN index for fast full-text search
CREATE INDEX IF NOT EXISTS idx_posts_search_vector ON posts USING GIN(search_vector);

-- Create trigger to auto-update search_vector on insert/update
CREATE OR REPLACE FUNCTION update_posts_search_vector() RETURNS trigger AS $$
BEGIN
  NEW.search_vector := 
    setweight(to_tsvector('spanish', coalesce(NEW.titulo, '')), 'A') ||
    setweight(to_tsvector('spanish', coalesce(NEW.contenido, '')), 'B');
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_posts_search_vector ON posts;
CREATE TRIGGER trg_posts_search_vector
  BEFORE INSERT OR UPDATE ON posts
  FOR EACH ROW
  EXECUTE FUNCTION update_posts_search_vector();

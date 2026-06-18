-- V13: Add full-text search support to events and research papers

-- =============================================
-- UNIVERSITY EVENTS
-- =============================================

ALTER TABLE university_events ADD COLUMN IF NOT EXISTS search_vector tsvector;

UPDATE university_events SET search_vector = 
  setweight(to_tsvector('spanish', coalesce(title, '')), 'A') ||
  setweight(to_tsvector('spanish', coalesce(description, '')), 'B');

CREATE INDEX IF NOT EXISTS idx_events_search_vector ON university_events USING GIN(search_vector);

CREATE OR REPLACE FUNCTION update_events_search_vector() RETURNS trigger AS $$
BEGIN
  NEW.search_vector := 
    setweight(to_tsvector('spanish', coalesce(NEW.title, '')), 'A') ||
    setweight(to_tsvector('spanish', coalesce(NEW.description, '')), 'B');
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_events_search_vector ON university_events;
CREATE TRIGGER trg_events_search_vector
  BEFORE INSERT OR UPDATE ON university_events
  FOR EACH ROW
  EXECUTE FUNCTION update_events_search_vector();

-- =============================================
-- RESEARCH PAPERS
-- =============================================

ALTER TABLE research_papers ADD COLUMN IF NOT EXISTS search_vector tsvector;

UPDATE research_papers SET search_vector = 
  setweight(to_tsvector('spanish', coalesce(title, '')), 'A') ||
  setweight(to_tsvector('spanish', coalesce(summary, '')), 'B');

CREATE INDEX IF NOT EXISTS idx_papers_search_vector ON research_papers USING GIN(search_vector);

CREATE OR REPLACE FUNCTION update_papers_search_vector() RETURNS trigger AS $$
BEGIN
  NEW.search_vector := 
    setweight(to_tsvector('spanish', coalesce(NEW.title, '')), 'A') ||
    setweight(to_tsvector('spanish', coalesce(NEW.summary, '')), 'B');
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_papers_search_vector ON research_papers;
CREATE TRIGGER trg_papers_search_vector
  BEFORE INSERT OR UPDATE ON research_papers
  FOR EACH ROW
  EXECUTE FUNCTION update_papers_search_vector();
